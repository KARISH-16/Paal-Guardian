package com.example.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.UUID

data class DiscoveredCanDevice(
    val device: BluetoothDevice,
    val name: String,
    val address: String,
    val rssi: Int
)

data class CanTelemetryData(
    val canId: String,
    val temperature: Double,
    val battery: Int,
    val timestamp: String,
    val isSafe: Boolean // true if 4.0 <= temp <= 8.0
)

enum class BleConnectionState {
    DISCONNECTED,
    SCANNING,
    CONNECTING,
    CONNECTED,
    SUBSCRIBED
}

class BluetoothLeManager(private val context: Context) {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var activeGatt: BluetoothGatt? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _connectionState = MutableStateFlow(BleConnectionState.DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    val connectedDeviceName = _connectedDeviceName.asStateFlow()

    private val _connectedCanId = MutableStateFlow<String?>(null)
    val connectedCanId = _connectedCanId.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredCanDevice>>(emptyList())
    val discoveredDevices = _discoveredDevices.asStateFlow()

    private val _telemetryFlow = MutableSharedFlow<CanTelemetryData>(replay = 1)
    val telemetryFlow = _telemetryFlow.asSharedFlow()

    private val _errorMessages = MutableSharedFlow<String>()
    val errorMessages = _errorMessages.asSharedFlow()

    val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val deviceName = device.name ?: result.scanRecord?.deviceName ?: ""

            // Filter for SmartCan_ prefix or accept any SmartCan device
            if (deviceName.startsWith("SmartCan", ignoreCase = true) || deviceName.contains("CAN", ignoreCase = true)) {
                val current = _discoveredDevices.value.toMutableList()
                val existingIndex = current.indexOfFirst { it.address == device.address }
                val item = DiscoveredCanDevice(
                    device = device,
                    name = deviceName.ifEmpty { "SmartCan_${device.address.takeLast(4)}" },
                    address = device.address,
                    rssi = result.rssi
                )
                if (existingIndex >= 0) {
                    current[existingIndex] = item
                } else {
                    current.add(item)
                }
                _discoveredDevices.value = current
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "BLE Scan failed with error code: $errorCode")
            _connectionState.value = BleConnectionState.DISCONNECTED
            scope.launch { _errorMessages.emit("Scan failed (error code: $errorCode)") }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.d(TAG, "onConnectionStateChange: status=$status, newState=$newState")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                _connectionState.value = BleConnectionState.CONNECTING
                activeGatt = gatt
                // Request MTU or discover services
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _connectionState.value = BleConnectionState.DISCONNECTED
                _connectedDeviceName.value = null
                _connectedCanId.value = null
                closeGatt()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.d(TAG, "onServicesDiscovered: status=$status")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(SERVICE_UUID)
                if (service != null) {
                    val characteristic = service.getCharacteristic(CHARACTERISTIC_UUID)
                    if (characteristic != null) {
                        // Subscribe to notifications
                        val success = gatt.setCharacteristicNotification(characteristic, true)
                        Log.d(TAG, "setCharacteristicNotification success=$success")

                        // Write to Client Characteristic Configuration Descriptor (CCCD)
                        val descriptor = characteristic.getDescriptor(CCCD_UUID)
                        if (descriptor != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                            } else {
                                @Suppress("DEPRECATION")
                                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                @Suppress("DEPRECATION")
                                gatt.writeDescriptor(descriptor)
                            }
                        }

                        // Also trigger initial read
                        gatt.readCharacteristic(characteristic)

                        _connectionState.value = BleConnectionState.SUBSCRIBED
                        _connectedDeviceName.value = gatt.device.name ?: "Smart Can"
                    } else {
                        Log.w(TAG, "Characteristic $CHARACTERISTIC_UUID not found")
                        _connectionState.value = BleConnectionState.CONNECTED
                    }
                } else {
                    Log.w(TAG, "Service $SERVICE_UUID not found")
                    _connectionState.value = BleConnectionState.CONNECTED
                }
            } else {
                scope.launch { _errorMessages.emit("Failed to discover Smart Can services") }
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                @Suppress("DEPRECATION")
                val value = characteristic.value
                parseAndEmitTelemetry(value)
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                parseAndEmitTelemetry(value)
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            @Suppress("DEPRECATION")
            val value = characteristic.value
            parseAndEmitTelemetry(value)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            parseAndEmitTelemetry(value)
        }
    }

    private fun parseAndEmitTelemetry(bytes: ByteArray?) {
        if (bytes == null || bytes.isEmpty()) return
        val jsonString = String(bytes, StandardCharsets.UTF_8).trim()
        Log.d(TAG, "Received BLE JSON: $jsonString")

        try {
            val json = JSONObject(jsonString)
            val canId = json.getString("canId")
            val temp = json.getDouble("temperature")
            val battery = json.getInt("battery")
            val timestamp = json.optString("timestamp", java.time.Instant.now().toString())

            _connectedCanId.value = canId
            val isSafe = temp in 4.0..8.0

            val telemetry = CanTelemetryData(
                canId = canId,
                temperature = temp,
                battery = battery,
                timestamp = timestamp,
                isSafe = isSafe
            )

            scope.launch {
                _telemetryFlow.emit(telemetry)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Invalid JSON received over BLE: '$jsonString'", e)
            scope.launch {
                _errorMessages.emit("Invalid Smart Can telemetry format")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null) {
            scope.launch { _errorMessages.emit("Bluetooth LE scanner not available") }
            return
        }

        _discoveredDevices.value = emptyList()
        _connectionState.value = BleConnectionState.SCANNING

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            scanner.startScan(null, settings, scanCallback)
        } catch (e: Exception) {
            Log.e(TAG, "startScan error", e)
            _connectionState.value = BleConnectionState.DISCONNECTED
            scope.launch { _errorMessages.emit(e.localizedMessage ?: "Failed to start BLE scan") }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        try {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (_: Exception) {}

        if (_connectionState.value == BleConnectionState.SCANNING) {
            _connectionState.value = BleConnectionState.DISCONNECTED
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        stopScan()
        closeGatt()

        _connectionState.value = BleConnectionState.CONNECTING
        activeGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            device.connectGatt(context, false, gattCallback)
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        stopScan()
        activeGatt?.disconnect()
        closeGatt()
        _connectionState.value = BleConnectionState.DISCONNECTED
        _connectedDeviceName.value = null
        _connectedCanId.value = null
    }

    @SuppressLint("MissingPermission")
    private fun closeGatt() {
        try {
            activeGatt?.close()
        } catch (_: Exception) {}
        activeGatt = null
    }

    companion object {
        private const val TAG = "SmartCanBLE"

        // Specified exact UUIDs from prompt:
        val SERVICE_UUID: UUID = UUID.fromString("0000A001-0000-1000-8000-00805F9B34FB")
        val CHARACTERISTIC_UUID: UUID = UUID.fromString("0000A002-0000-1000-8000-00805F9B34FB")
        val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
}
