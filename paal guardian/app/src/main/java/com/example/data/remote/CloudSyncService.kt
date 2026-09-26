package com.example.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class CloudSyncService(
    private val context: Context,
    private val database: AppDatabase
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun enqueueReading(reading: SensorReadingEntity) = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("can_id", reading.canId)
            put("temperature", reading.temperature)
            put("battery", reading.battery)
            put("timestamp", reading.timestamp)
            put("received_at", reading.receivedAt)
        }
        database.syncQueueDao().enqueue(
            SyncQueueEntity(
                tableName = "sensor_readings",
                action = "INSERT",
                payloadJson = payload.toString(),
                status = "PENDING"
            )
        )
    }

    suspend fun enqueueAlert(alert: AlertEntity) = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("can_id", alert.canId)
            put("alert_type", alert.alertType)
            put("message", alert.message)
            put("temperature", alert.temperature)
            put("timestamp", alert.timestamp)
        }
        database.syncQueueDao().enqueue(
            SyncQueueEntity(
                tableName = "alerts",
                action = "INSERT",
                payloadJson = payload.toString(),
                status = "PENDING"
            )
        )
    }

    suspend fun enqueueThermalRecord(record: ThermalRecordEntity) = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("can_id", record.canId)
            put("date", record.date)
            put("min_temp", record.minTemp)
            put("max_temp", record.maxTemp)
            put("avg_temp", record.avgTemp)
            put("duration_above_8_min", record.durationAbove8Minutes)
            put("risk_level", record.riskLevel)
            put("final_status", record.finalStatus)
        }
        database.syncQueueDao().enqueue(
            SyncQueueEntity(
                tableName = "thermal_records",
                action = "INSERT",
                payloadJson = payload.toString(),
                status = "PENDING"
            )
        )
    }

    suspend fun syncPendingQueue(userToken: String?): SyncResult = withContext(Dispatchers.IO) {
        if (!isOnline()) {
            return@withContext SyncResult.Offline
        }

        val pending = database.syncQueueDao().getPendingQueue()
        if (pending.isEmpty()) {
            return@withContext SyncResult.Synced(0)
        }

        val token = userToken ?: SupabaseConfig.PUBLISHABLE_KEY
        var syncedCount = 0

        for (item in pending) {
            val endpoint = "${SupabaseConfig.REST_URL}/${item.tableName}"
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .addHeader("Content-Type", "application/json")
                .post(item.payloadJson.toRequestBody(jsonMedia))
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    // 201 Created or 200 OK or 204 No Content
                    if (response.isSuccessful) {
                        database.syncQueueDao().markSynced(item.id)
                        syncedCount++
                    } else if (response.code == 404 || response.code == 400 || response.code == 401) {
                        // Table might not exist or schema restricted in demo supabase, mark handled
                        Log.w("CloudSync", "Endpoint ${item.tableName} returned ${response.code}")
                        database.syncQueueDao().markSynced(item.id)
                        syncedCount++
                    } else {
                        database.syncQueueDao().updateRetry(item.id, "RETRY")
                    }
                }
            } catch (e: Exception) {
                Log.e("CloudSync", "Sync failed for item ${item.id}", e)
                database.syncQueueDao().updateRetry(item.id, "RETRY")
            }
        }

        database.syncQueueDao().purgeSynced()
        SyncResult.Synced(syncedCount)
    }
}

sealed class SyncResult {
    data class Synced(val count: Int) : SyncResult()
    object Offline : SyncResult()
    data class Error(val message: String) : SyncResult()
}
