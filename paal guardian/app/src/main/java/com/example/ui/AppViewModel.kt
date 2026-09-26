package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ble.BleConnectionState
import com.example.ble.DiscoveredCanDevice
import com.example.data.local.entity.*
import com.example.data.remote.AuthResult
import com.example.data.remote.SyncResult
import com.example.data.repository.AppRepository
import com.example.demo.DemoSessionManager
import com.example.ml.MilkQualityInputs
import com.example.ml.MlInferenceResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ScreenRoute(val route: String) {
    object Splash : ScreenRoute("splash")
    object RoleSelection : ScreenRoute("role_selection")
    object FarmerLogin : ScreenRoute("farmer_login")
    object FarmerRegister : ScreenRoute("farmer_register")
    object CenterLogin : ScreenRoute("center_login")
    object CenterRegister : ScreenRoute("center_register")
    object FarmerDashboard : ScreenRoute("farmer_dashboard")
    object MyCans : ScreenRoute("my_cans")
    object ConnectCan : ScreenRoute("connect_can")
    object LiveCanData : ScreenRoute("live_can_data")
    object RecordsHistory : ScreenRoute("records_history")
    object ThermalPassport : ScreenRoute("thermal_passport")
    object FarmerProfile : ScreenRoute("farmer_profile")
    object AlertsList : ScreenRoute("alerts_list")
    object CenterDashboard : ScreenRoute("center_dashboard")
    object CenterFarmersList : ScreenRoute("center_farmers_list")
    object CenterAllCans : ScreenRoute("center_all_cans")
    object CenterAnalytics : ScreenRoute("center_analytics")
    object CenterProfile : ScreenRoute("center_profile")
    // Demo routes (distinct isolated demo mode flow)
    object DemoFarmerDashboard : ScreenRoute("demo_farmer_dashboard")
    object DemoConnectCan : ScreenRoute("demo_connect_can")
    object DemoLiveDashboard : ScreenRoute("demo_live_dashboard")
    object DemoCentreDashboard : ScreenRoute("demo_centre_dashboard")
    // ML Analysis
    object MilkQualityMl : ScreenRoute("milk_quality_ml")
}

data class AuthUiState(
    val identifier: String = "",
    val password: String = "",
    val name: String = "",
    val phone: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    val repository = AppRepository.getInstance(application)
    val demoManager = DemoSessionManager.instance

    val currentUser: StateFlow<UserEntity?> = repository.currentUserFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allCans: StateFlow<List<CanEntity>> = repository.allCansFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAlerts: StateFlow<List<AlertEntity>> = repository.allAlertsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bleConnectionState: StateFlow<BleConnectionState> = repository.bleManager.connectionState
    val discoveredDevices: StateFlow<List<DiscoveredCanDevice>> = repository.bleManager.discoveredDevices
    val connectedDeviceName: StateFlow<String?> = repository.bleManager.connectedDeviceName
    val connectedCanId: StateFlow<String?> = repository.bleManager.connectedCanId

    // Selected Can for Detail / History / Thermal Passport
    private val _selectedCanId = MutableStateFlow<String?>(null)
    val selectedCanId = _selectedCanId.asStateFlow()

    // Navigation State
    private val _currentRoute = MutableStateFlow<ScreenRoute>(ScreenRoute.Splash)
    val currentRoute = _currentRoute.asStateFlow()

    // Auth State
    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState = _authUiState.asStateFlow()

    // Temperature Warning Banner / Dialog state (for real flow)
    private val _activeWarningAlert = MutableStateFlow<AlertEntity?>(null)
    val activeWarningAlert = _activeWarningAlert.asStateFlow()

    // ML Evaluation result state
    private val _mlInferenceResult = MutableStateFlow<MlInferenceResult?>(null)
    val mlInferenceResult = _mlInferenceResult.asStateFlow()

    // Sync notification message
    private val _syncMessage = MutableSharedFlow<String>()
    val syncMessage = _syncMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.warningAlertEvent.collect { alert ->
                _activeWarningAlert.value = alert
            }
        }

        viewModelScope.launch {
            repository.currentUserFlow.collect { user ->
                if (user != null) {
                    val cans = repository.allCansFlow.first()
                    if (_selectedCanId.value == null && cans.isNotEmpty()) {
                        _selectedCanId.value = cans.first().canId
                    }
                }
            }
        }
    }

    fun navigateTo(route: ScreenRoute) {
        _currentRoute.value = route
    }

    fun selectCan(canId: String) {
        _selectedCanId.value = canId
    }

    fun dismissWarningAlert() {
        _activeWarningAlert.value = null
    }

    fun startFarmerDemo() {
        demoManager.startDemoSession("farmer")
        navigateTo(ScreenRoute.DemoFarmerDashboard)
    }

    fun startCenterDemo() {
        demoManager.startDemoSession("center")
        navigateTo(ScreenRoute.DemoCentreDashboard)
    }

    fun exitDemo() {
        demoManager.stopDemoSession()
        navigateTo(ScreenRoute.RoleSelection)
    }

    fun updateAuthIdentifier(value: String) {
        _authUiState.value = _authUiState.value.copy(identifier = value, errorMessage = null)
    }

    fun updateAuthPassword(value: String) {
        _authUiState.value = _authUiState.value.copy(password = value, errorMessage = null)
    }

    fun updateAuthName(value: String) {
        _authUiState.value = _authUiState.value.copy(name = value, errorMessage = null)
    }

    fun updateAuthPhone(value: String) {
        _authUiState.value = _authUiState.value.copy(phone = value, errorMessage = null)
    }

    fun login(role: String) {
        val state = _authUiState.value
        if (state.identifier.isBlank() || state.password.isBlank()) {
            _authUiState.value = state.copy(errorMessage = "Please enter your phone/email and password")
            return
        }

        viewModelScope.launch {
            _authUiState.value = state.copy(isLoading = true, errorMessage = null)
            val result = repository.login(state.identifier, state.password)
            when (result) {
                is AuthResult.Success -> {
                    _authUiState.value = AuthUiState()
                    if (result.user.role == "collection_center") {
                        navigateTo(ScreenRoute.CenterDashboard)
                    } else {
                        navigateTo(ScreenRoute.FarmerDashboard)
                    }
                }
                is AuthResult.Error -> {
                    _authUiState.value = state.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun register(role: String) {
        val state = _authUiState.value
        if (state.identifier.isBlank() || state.password.isBlank() || state.name.isBlank()) {
            _authUiState.value = state.copy(errorMessage = "Please fill in all required fields")
            return
        }

        viewModelScope.launch {
            _authUiState.value = state.copy(isLoading = true, errorMessage = null)
            val result = repository.register(
                inputIdentifier = state.identifier,
                pass = state.password,
                name = state.name,
                phone = state.phone.ifEmpty { state.identifier },
                role = role
            )
            when (result) {
                is AuthResult.Success -> {
                    _authUiState.value = AuthUiState()
                    if (role == "collection_center") {
                        navigateTo(ScreenRoute.CenterDashboard)
                    } else {
                        navigateTo(ScreenRoute.FarmerDashboard)
                    }
                }
                is AuthResult.Error -> {
                    _authUiState.value = state.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _selectedCanId.value = null
            navigateTo(ScreenRoute.RoleSelection)
        }
    }

    fun startBleScan() {
        repository.bleManager.startScan()
    }

    fun stopBleScan() {
        repository.bleManager.stopScan()
    }

    fun connectDevice(device: DiscoveredCanDevice) {
        repository.bleManager.connectToDevice(device.device)
        val extractedId = if (device.name.startsWith("SmartCan_", ignoreCase = true)) {
            device.name.substringAfter("SmartCan_")
        } else {
            device.name
        }
        _selectedCanId.value = extractedId
        viewModelScope.launch {
            repository.registerCan(extractedId)
        }
    }

    fun disconnectCan() {
        repository.bleManager.disconnect()
    }

    fun syncNow() {
        viewModelScope.launch {
            val res = repository.syncNow()
            val msg = when (res) {
                is SyncResult.Synced -> "Successfully synchronized ${res.count} records with cloud"
                is SyncResult.Offline -> "Device offline. Data saved locally and will auto-sync when online."
                is SyncResult.Error -> "Sync warning: ${res.message}"
            }
            _syncMessage.emit(msg)
        }
    }

    fun evaluateMl(canId: String, inputs: MilkQualityInputs?) {
        viewModelScope.launch {
            val result = repository.evaluateMlInference(canId, inputs)
            _mlInferenceResult.value = result
        }
    }

    fun getReadingsForCan(canId: String): Flow<List<SensorReadingEntity>> =
        repository.getReadingsForCan(canId)

    fun getLatestReadingForCan(canId: String): Flow<SensorReadingEntity?> =
        repository.getLatestReadingForCan(canId)

    fun getCanById(canId: String): Flow<CanEntity?> =
        repository.getCanById(canId)

    fun getThermalRecordsForCan(canId: String): Flow<List<ThermalRecordEntity>> =
        repository.getThermalRecordsForCan(canId)

    fun getAllFarmers(): Flow<List<UserEntity>> =
        repository.getAllFarmers()
}
