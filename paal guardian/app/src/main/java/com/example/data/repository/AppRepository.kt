package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.ble.BluetoothLeManager
import com.example.ble.CanTelemetryData
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.remote.AuthResult
import com.example.data.remote.CloudSyncService
import com.example.data.remote.SupabaseAuthService
import com.example.data.remote.SyncResult
import com.example.ml.MilkQualityInputs
import com.example.ml.MilkQualityInferenceEngine
import com.example.ml.MlInferenceResult
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class AppRepository(
    private val context: Context,
    private val database: AppDatabase,
    val bleManager: BluetoothLeManager,
    private val authService: SupabaseAuthService,
    private val syncService: CloudSyncService,
    private val notificationHelper: NotificationHelper
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val prefs: SharedPreferences =
        context.getSharedPreferences("paal_guardian_prefs", Context.MODE_PRIVATE)

    val currentUserFlow: Flow<UserEntity?> = database.userDao().getCurrentUserFlow()
    val allCansFlow: Flow<List<CanEntity>> = database.canDao().getAllCansFlow()
    val allAlertsFlow: Flow<List<AlertEntity>> = database.alertDao().getAllAlertsFlow()

    private val _warningAlertEvent = MutableSharedFlow<AlertEntity>(replay = 0)
    val warningAlertEvent = _warningAlertEvent.asSharedFlow()

    init {
        // Observe BLE telemetry stream from Smart Can
        scope.launch {
            bleManager.telemetryFlow.collect { telemetry ->
                handleIncomingTelemetry(telemetry)
            }
        }
    }

    private suspend fun handleIncomingTelemetry(telemetry: CanTelemetryData) {
        val user = database.userDao().getCurrentUser()
        val farmerId = user?.id ?: "unknown_farmer"
        val farmerName = user?.name ?: "Ramesh"

        // 1. Update or Insert Can status
        val status = if (telemetry.temperature > 8.0) "Warning" else "Normal"
        val canEntity = CanEntity(
            canId = telemetry.canId,
            farmerId = farmerId,
            farmerName = farmerName,
            status = status,
            lastTemperature = telemetry.temperature,
            lastBattery = telemetry.battery,
            lastSeenTimestamp = telemetry.timestamp,
            isConnected = true,
            updatedAt = System.currentTimeMillis()
        )
        database.canDao().insertOrUpdateCan(canEntity)

        // 2. Insert real sensor reading into SQLite
        val reading = SensorReadingEntity(
            canId = telemetry.canId,
            temperature = telemetry.temperature,
            battery = telemetry.battery,
            timestamp = telemetry.timestamp,
            receivedAt = System.currentTimeMillis(),
            isSynced = false
        )
        database.sensorReadingDao().insertReading(reading)
        syncService.enqueueReading(reading)

        // 3. Check Temperature Warning (>8°C)
        if (telemetry.temperature > 8.0) {
            val alertMsg = "Temperature exceeded the recommended range. Milk quality may be at risk."
            val alert = AlertEntity(
                canId = telemetry.canId,
                alertType = "TEMPERATURE_HIGH",
                message = alertMsg,
                temperature = telemetry.temperature,
                timestamp = telemetry.timestamp,
                createdAt = System.currentTimeMillis(),
                isAcknowledged = false,
                isSynced = false
            )
            val alertId = database.alertDao().insertAlert(alert)
            val savedAlert = alert.copy(id = alertId)

            // Trigger offline Android system notification + warning sound
            notificationHelper.triggerTemperatureWarningNotification(telemetry.canId, telemetry.temperature)
            _warningAlertEvent.emit(savedAlert)
            syncService.enqueueAlert(savedAlert)
        }

        // 4. Update Rule-Based Thermal Record
        calculateAndSaveThermalRecord(telemetry.canId)

        // 5. If online, trigger background sync
        if (syncService.isOnline()) {
            syncService.syncPendingQueue(user?.token)
        }
    }

    private suspend fun calculateAndSaveThermalRecord(canId: String) {
        val readings = database.sensorReadingDao().getRecentReadings(canId)
        if (readings.isEmpty()) return

        val temps = readings.map { it.temperature }
        val minTemp = temps.minOrNull() ?: 0.0
        val maxTemp = temps.maxOrNull() ?: 0.0
        val avgTemp = temps.average()

        // Time above 8 C calculation: roughly count intervals of high readings
        val readingsAbove8 = readings.filter { it.temperature > 8.0 }.size
        // Assuming ~1-2 min per reading interval
        val durationAbove8Min = (readingsAbove8 * 2).toLong()

        val riskLevel = when {
            readingsAbove8 == 0 -> "LOW"
            durationAbove8Min < 30 -> "MODERATE"
            else -> "HIGH"
        }

        val finalStatus = if (riskLevel == "LOW") "Safe Range" else "Thermal Deviation"
        val todayStr = LocalDate.now().toString()

        val record = ThermalRecordEntity(
            canId = canId,
            date = todayStr,
            minTemp = minTemp,
            maxTemp = maxTemp,
            avgTemp = avgTemp,
            durationAbove8Minutes = durationAbove8Min,
            riskLevel = riskLevel,
            finalStatus = finalStatus,
            recordedAt = System.currentTimeMillis(),
            isSynced = false
        )
        database.thermalRecordDao().insertThermalRecord(record)
        syncService.enqueueThermalRecord(record)
    }

    fun getReadingsForCan(canId: String): Flow<List<SensorReadingEntity>> =
        database.sensorReadingDao().getReadingsForCanFlow(canId)

    fun getLatestReadingForCan(canId: String): Flow<SensorReadingEntity?> =
        database.sensorReadingDao().getLatestReadingFlow(canId)

    fun getCanById(canId: String): Flow<CanEntity?> =
        database.canDao().getCanByIdFlow(canId)

    fun getThermalRecordsForCan(canId: String): Flow<List<ThermalRecordEntity>> =
        database.thermalRecordDao().getRecordsForCanFlow(canId)

    fun getAllThermalRecords(): Flow<List<ThermalRecordEntity>> =
        database.thermalRecordDao().getAllThermalRecordsFlow()

    fun getAllFarmers(): Flow<List<UserEntity>> =
        database.userDao().getAllFarmersFlow()

    suspend fun registerCan(canId: String) {
        val user = database.userDao().getCurrentUser()
        val can = CanEntity(
            canId = canId,
            farmerId = user?.id ?: "farmer_01",
            farmerName = user?.name ?: "Ramesh",
            status = "Normal",
            lastTemperature = null,
            lastBattery = null,
            lastSeenTimestamp = null,
            isConnected = false
        )
        database.canDao().insertOrUpdateCan(can)
    }

    suspend fun evaluateMlInference(canId: String, inputs: MilkQualityInputs?): MlInferenceResult {
        val result = MilkQualityInferenceEngine.evaluate(
            canId = canId,
            inputs = inputs,
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )

        if (result.isAvailable && result.prediction != null && result.confidence != null) {
            val entity = MlPredictionEntity(
                canId = canId,
                timestamp = result.timestamp ?: "",
                prediction = result.prediction,
                confidence = result.confidence,
                inputsJson = inputs?.toString() ?: "",
                recordedAt = System.currentTimeMillis(),
                isSynced = false
            )
            database.mlPredictionDao().insertMlPrediction(entity)
        }
        return result
    }

    suspend fun syncNow(): SyncResult {
        val user = database.userDao().getCurrentUser()
        return syncService.syncPendingQueue(user?.token)
    }

    suspend fun login(inputIdentifier: String, pass: String): AuthResult {
        val result = authService.login(inputIdentifier, pass)
        if (result is AuthResult.Success) {
            val user = UserEntity(
                id = result.user.id,
                email = result.user.email,
                name = result.user.name,
                phone = result.user.phone,
                role = result.user.role,
                token = result.user.accessToken
            )
            database.userDao().clearUsers()
            database.userDao().insertUser(user)
            prefs.edit().putString("saved_role", user.role).apply()
        }
        return result
    }

    suspend fun register(
        inputIdentifier: String,
        pass: String,
        name: String,
        phone: String,
        role: String
    ): AuthResult {
        val result = authService.register(inputIdentifier, pass, name, phone, role)
        if (result is AuthResult.Success) {
            val user = UserEntity(
                id = result.user.id,
                email = result.user.email,
                name = result.user.name,
                phone = result.user.phone,
                role = result.user.role,
                token = result.user.accessToken
            )
            database.userDao().clearUsers()
            database.userDao().insertUser(user)
            prefs.edit().putString("saved_role", user.role).apply()
        }
        return result
    }

    suspend fun logout() {
        bleManager.disconnect()
        database.canDao().resetAllConnections()
        database.userDao().clearUsers()
        prefs.edit().remove("saved_role").apply()
    }

    suspend fun acknowledgeAllAlerts() {
        database.alertDao().acknowledgeAllAlerts()
    }

    companion object {
        @Volatile
        private var INSTANCE: AppRepository? = null

        fun getInstance(context: Context): AppRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getInstance(context)
                val ble = BluetoothLeManager(context)
                val auth = SupabaseAuthService()
                val sync = CloudSyncService(context, db)
                val notif = NotificationHelper(context)
                val repo = AppRepository(context, db, ble, auth, sync, notif)
                INSTANCE = repo
                repo
            }
        }
    }
}
