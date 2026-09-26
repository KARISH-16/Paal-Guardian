package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val name: String,
    val phone: String,
    val role: String, // "farmer" or "collection_center"
    val token: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cans")
data class CanEntity(
    @PrimaryKey val canId: String,
    val farmerId: String,
    val farmerName: String,
    val status: String = "Normal", // "Normal", "Warning", "Offline"
    val lastTemperature: Double? = null,
    val lastBattery: Int? = null,
    val lastSeenTimestamp: String? = null,
    val isConnected: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sensor_readings")
data class SensorReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val canId: String,
    val temperature: Double,
    val battery: Int,
    val timestamp: String,
    val receivedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val canId: String,
    val alertType: String, // "TEMPERATURE_HIGH", "BATTERY_LOW", etc.
    val message: String,
    val temperature: Double,
    val timestamp: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isAcknowledged: Boolean = false,
    val isSynced: Boolean = false
)

@Entity(tableName = "thermal_records")
data class ThermalRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val canId: String,
    val date: String, // "2026-09-07"
    val minTemp: Double,
    val maxTemp: Double,
    val avgTemp: Double,
    val durationAbove8Minutes: Long,
    val riskLevel: String, // "LOW", "MODERATE", "HIGH"
    val finalStatus: String, // "Safe Range", "Thermal Deviation"
    val recordedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "ml_predictions")
data class MlPredictionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val canId: String,
    val timestamp: String,
    val prediction: String, // "Good", "Warning", "Poor"
    val confidence: Double,
    val inputsJson: String,
    val recordedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tableName: String,
    val action: String, // "INSERT", "UPDATE"
    val payloadJson: String,
    val status: String = "PENDING", // "PENDING", "SYNCED", "FAILED"
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
