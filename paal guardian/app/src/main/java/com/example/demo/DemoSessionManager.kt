package com.example.demo

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DemoTelemetryPoint(
    val index: Int,
    val temperature: Double,
    val battery: Int,
    val timestamp: String,
    val timeLabel: String,
    val isWarning: Boolean
)

data class DemoAlert(
    val id: Long,
    val canId: String,
    val temperature: Double,
    val message: String,
    val timestamp: String
)

class DemoSessionManager {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var telemetryJob: Job? = null

    // Exact sequences specified in user prompt:
    val temperatureSequence = listOf(
        5.2, 5.4, 5.7, 5.9, 6.1, 6.3, 6.6, 7.0,
        7.4, 8.2, 8.7, 9.1, 8.6, 7.8, 7.2, 6.8
    )

    val batterySequence = listOf(
        92, 91, 90, 89, 88, 87, 86, 85
    )

    private val _isDemoActive = MutableStateFlow(false)
    val isDemoActive = _isDemoActive.asStateFlow()
    val isDemoMode = isDemoActive

    private val _demoRole = MutableStateFlow<String?>("farmer") // "farmer" or "center"
    val demoRole = _demoRole.asStateFlow()

    // Can Connection State in Demo
    private val _demoCanId = MutableStateFlow("CAN00123")
    val demoCanId = _demoCanId.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _isDiscovered = MutableStateFlow(false)
    val isDiscovered = _isDiscovered.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting = _isConnecting.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    // Live Telemetry Index & History
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex = _currentIndex.asStateFlow()

    private val _currentTemperature = MutableStateFlow(5.2)
    val currentTemperature = _currentTemperature.asStateFlow()

    private val _currentBattery = MutableStateFlow(92)
    val currentBattery = _currentBattery.asStateFlow()

    private val _telemetryHistory = MutableStateFlow<List<DemoTelemetryPoint>>(emptyList())
    val telemetryHistory = _telemetryHistory.asStateFlow()

    private val _activeWarningAlert = MutableStateFlow<DemoAlert?>(null)
    val activeWarningAlert = _activeWarningAlert.asStateFlow()

    private val _alertsList = MutableStateFlow<List<DemoAlert>>(emptyList())
    val alertsList = _alertsList.asStateFlow()

    val canId: String = "CAN00123"

    fun startDemoSession(role: String) {
        _isDemoActive.value = true
        _demoRole.value = role
        _demoCanId.value = "CAN00123"
        _isScanning.value = false
        _isDiscovered.value = false
        _isConnecting.value = false
        _isConnected.value = false
        _currentIndex.value = 0
        _currentTemperature.value = temperatureSequence[0]
        _currentBattery.value = batterySequence[0]
        _telemetryHistory.value = emptyList()
        _alertsList.value = emptyList()
        _activeWarningAlert.value = null
        telemetryJob?.cancel()
    }

    fun startDemoScan() {
        _isScanning.value = true
        _isDiscovered.value = false
        scope.launch {
            delay(1200) // Realistic Bluetooth discovery delay
            _isScanning.value = false
            _isDiscovered.value = true
        }
    }

    fun selectAndConnectDemoCan(onConnected: () -> Unit) {
        _isConnecting.value = true
        scope.launch {
            delay(1500) // Realistic Bluetooth connecting handshake
            _isConnecting.value = false
            _isConnected.value = true
            startTelemetryStream()
            onConnected()
        }
    }

    fun directConnectDemoCan() {
        _isDiscovered.value = true
        _isConnecting.value = false
        _isConnected.value = true
        startTelemetryStream()
    }

    private fun startTelemetryStream() {
        telemetryJob?.cancel()
        _telemetryHistory.value = emptyList()
        _alertsList.value = emptyList()

        telemetryJob = scope.launch {
            val baseTime = LocalDateTime.now().minusMinutes(temperatureSequence.size.toLong() * 2)
            val history = mutableListOf<DemoTelemetryPoint>()
            val alerts = mutableListOf<DemoAlert>()

            for (i in temperatureSequence.indices) {
                _currentIndex.value = i
                val temp = temperatureSequence[i]
                val batt = batterySequence.getOrElse(i) { batterySequence.last() }
                _currentTemperature.value = temp
                _currentBattery.value = batt

                val pointTime = baseTime.plusMinutes((i * 2).toLong())
                val timeStr = pointTime.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US))
                val fullIso = pointTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val isWarning = temp > 8.0

                val point = DemoTelemetryPoint(
                    index = i,
                    temperature = temp,
                    battery = batt,
                    timestamp = fullIso,
                    timeLabel = timeStr,
                    isWarning = isWarning
                )
                history.add(point)
                _telemetryHistory.value = history.toList()

                // Trigger warning when temp reaches 8.2, 8.7, 9.1
                if (isWarning) {
                    val alert = DemoAlert(
                        id = System.currentTimeMillis() + i,
                        canId = "CAN00123",
                        temperature = temp,
                        message = "Temperature exceeded the recommended range. Milk quality may be at risk.",
                        timestamp = fullIso
                    )
                    alerts.add(0, alert)
                    _alertsList.value = alerts.toList()
                    _activeWarningAlert.value = alert
                }

                delay(2600) // Advance automatically through readings every 2.6s
            }
        }
    }

    fun dismissActiveWarning() {
        _activeWarningAlert.value = null
    }

    fun dismissWarningAlert() {
        dismissActiveWarning()
    }

    fun stopDemoSession() {
        telemetryJob?.cancel()
        _isDemoActive.value = false
        _isConnected.value = false
        _isDiscovered.value = false
        _isConnecting.value = false
        _telemetryHistory.value = emptyList()
    }

    companion object {
        val instance = DemoSessionManager()
    }
}
