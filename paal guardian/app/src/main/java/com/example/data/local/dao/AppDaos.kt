package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Query("SELECT * FROM users WHERE role = 'farmer'")
    fun getAllFarmersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clearUsers()
}

@Dao
interface CanDao {
    @Query("SELECT * FROM cans ORDER BY updatedAt DESC")
    fun getAllCansFlow(): Flow<List<CanEntity>>

    @Query("SELECT * FROM cans WHERE farmerId = :farmerId ORDER BY updatedAt DESC")
    fun getCansByFarmerFlow(farmerId: String): Flow<List<CanEntity>>

    @Query("SELECT * FROM cans WHERE canId = :canId LIMIT 1")
    fun getCanByIdFlow(canId: String): Flow<CanEntity?>

    @Query("SELECT * FROM cans WHERE canId = :canId LIMIT 1")
    suspend fun getCanById(canId: String): CanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCan(can: CanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCans(cans: List<CanEntity>)

    @Query("UPDATE cans SET isConnected = :isConnected WHERE canId = :canId")
    suspend fun updateConnectionStatus(canId: String, isConnected: Boolean)

    @Query("UPDATE cans SET isConnected = 0")
    suspend fun resetAllConnections()

    @Delete
    suspend fun deleteCan(can: CanEntity)
}

@Dao
interface SensorReadingDao {
    @Query("SELECT * FROM sensor_readings WHERE canId = :canId ORDER BY receivedAt DESC")
    fun getReadingsForCanFlow(canId: String): Flow<List<SensorReadingEntity>>

    @Query("SELECT * FROM sensor_readings WHERE canId = :canId ORDER BY receivedAt DESC LIMIT 1")
    fun getLatestReadingFlow(canId: String): Flow<SensorReadingEntity?>

    @Query("SELECT * FROM sensor_readings WHERE canId = :canId ORDER BY receivedAt DESC LIMIT 100")
    suspend fun getRecentReadings(canId: String): List<SensorReadingEntity>

    @Query("SELECT * FROM sensor_readings WHERE isSynced = 0")
    suspend fun getUnsyncedReadings(): List<SensorReadingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: SensorReadingEntity): Long

    @Query("UPDATE sensor_readings SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)
}

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY createdAt DESC")
    fun getAllAlertsFlow(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE canId = :canId ORDER BY createdAt DESC")
    fun getAlertsForCanFlow(canId: String): Flow<List<AlertEntity>>

    @Query("SELECT COUNT(*) FROM alerts WHERE isAcknowledged = 0")
    fun getUnacknowledgedAlertsCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity): Long

    @Query("UPDATE alerts SET isAcknowledged = 1 WHERE id = :id")
    suspend fun acknowledgeAlert(id: Long)

    @Query("UPDATE alerts SET isAcknowledged = 1")
    suspend fun acknowledgeAllAlerts()
}

@Dao
interface ThermalRecordDao {
    @Query("SELECT * FROM thermal_records WHERE canId = :canId ORDER BY recordedAt DESC")
    fun getRecordsForCanFlow(canId: String): Flow<List<ThermalRecordEntity>>

    @Query("SELECT * FROM thermal_records ORDER BY recordedAt DESC")
    fun getAllThermalRecordsFlow(): Flow<List<ThermalRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThermalRecord(record: ThermalRecordEntity): Long
}

@Dao
interface MlPredictionDao {
    @Query("SELECT * FROM ml_predictions WHERE canId = :canId ORDER BY recordedAt DESC")
    fun getPredictionsForCanFlow(canId: String): Flow<List<MlPredictionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMlPrediction(prediction: MlPredictionEntity): Long
}

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingQueue(): List<SyncQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: SyncQueueEntity): Long

    @Query("UPDATE sync_queue SET status = 'SYNCED' WHERE id = :id")
    suspend fun markSynced(id: Long)

    @Query("UPDATE sync_queue SET retryCount = retryCount + 1, status = :status WHERE id = :id")
    suspend fun updateRetry(id: Long, status: String)

    @Query("DELETE FROM sync_queue WHERE status = 'SYNCED'")
    suspend fun purgeSynced()
}

@Dao
interface AppSettingDao {
    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingEntity)
}
