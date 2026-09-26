package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.local.entity.AlertEntity
import com.example.ui.AppViewModel
import com.example.ui.ScreenRoute
import com.example.ui.components.WarningAlertDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle notification click intent extras
        intent?.let {
            val canId = it.getStringExtra("EXTRA_CAN_ID")
            if (canId != null) {
                viewModel.selectCan(canId)
                viewModel.navigateTo(ScreenRoute.LiveCanData)
            }
        }

        setContent {
            MyApplicationTheme {
                // Request Notification Permission for Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val notifPermissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { /* granted */ }

                    LaunchedEffect(Unit) {
                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    PaalGuardianRoot(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun PaalGuardianRoot(viewModel: AppViewModel) {
    val currentRoute by viewModel.currentRoute.collectAsState()
    val activeWarningAlert by viewModel.activeWarningAlert.collectAsState()
    val demoWarningAlert by viewModel.demoManager.activeWarningAlert.collectAsState()
    val selectedCanId by viewModel.selectedCanId.collectAsState()
    val cans by viewModel.allCans.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Handle back button behavior according to current screen
    BackHandler(enabled = currentRoute !is ScreenRoute.Splash && currentRoute !is ScreenRoute.RoleSelection) {
        when (currentRoute) {
            is ScreenRoute.FarmerDashboard -> viewModel.navigateTo(ScreenRoute.RoleSelection)
            is ScreenRoute.CenterDashboard -> viewModel.navigateTo(ScreenRoute.RoleSelection)
            is ScreenRoute.MyCans, is ScreenRoute.RecordsHistory, is ScreenRoute.ThermalPassport, is ScreenRoute.FarmerProfile, is ScreenRoute.AlertsList -> {
                viewModel.navigateTo(ScreenRoute.FarmerDashboard)
            }
            is ScreenRoute.LiveCanData -> viewModel.navigateTo(ScreenRoute.MyCans)
            is ScreenRoute.ConnectCan -> viewModel.navigateTo(ScreenRoute.MyCans)
            is ScreenRoute.FarmerLogin, is ScreenRoute.FarmerRegister, is ScreenRoute.CenterLogin, is ScreenRoute.CenterRegister -> {
                viewModel.navigateTo(ScreenRoute.RoleSelection)
            }
            // Demo routes back navigation
            is ScreenRoute.DemoFarmerDashboard -> viewModel.exitDemo()
            is ScreenRoute.DemoConnectCan -> viewModel.navigateTo(ScreenRoute.DemoFarmerDashboard)
            is ScreenRoute.DemoLiveDashboard -> viewModel.navigateTo(ScreenRoute.DemoFarmerDashboard)
            is ScreenRoute.DemoCentreDashboard -> viewModel.exitDemo()
            is ScreenRoute.MilkQualityMl -> {
                if (viewModel.demoManager.isDemoActive.value) {
                    viewModel.navigateTo(ScreenRoute.DemoLiveDashboard)
                } else {
                    viewModel.navigateTo(ScreenRoute.FarmerDashboard)
                }
            }
            else -> viewModel.navigateTo(ScreenRoute.RoleSelection)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentRoute) {
            is ScreenRoute.Splash -> {
                SplashScreen(
                    onGetStarted = {
                        if (currentUser != null) {
                            if (currentUser?.role == "collection_center") {
                                viewModel.navigateTo(ScreenRoute.CenterDashboard)
                            } else {
                                viewModel.navigateTo(ScreenRoute.FarmerDashboard)
                            }
                        } else {
                            viewModel.navigateTo(ScreenRoute.RoleSelection)
                        }
                    },
                    onStartDemo = {
                        viewModel.startFarmerDemo()
                    }
                )
            }

            is ScreenRoute.RoleSelection -> {
                RoleSelectionScreen(
                    onSelectFarmer = { viewModel.navigateTo(ScreenRoute.FarmerLogin) },
                    onSelectCenter = { viewModel.navigateTo(ScreenRoute.CenterLogin) }
                )
            }

            is ScreenRoute.FarmerLogin -> {
                FarmerLoginScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(ScreenRoute.RoleSelection) },
                    onNavigateRegister = { viewModel.navigateTo(ScreenRoute.FarmerRegister) }
                )
            }

            is ScreenRoute.FarmerRegister -> {
                FarmerRegisterScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(ScreenRoute.FarmerLogin) },
                    onNavigateLogin = { viewModel.navigateTo(ScreenRoute.FarmerLogin) }
                )
            }

            is ScreenRoute.CenterLogin -> {
                CenterLoginScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(ScreenRoute.RoleSelection) },
                    onNavigateRegister = { viewModel.navigateTo(ScreenRoute.CenterRegister) }
                )
            }

            is ScreenRoute.CenterRegister -> {
                CenterRegisterScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(ScreenRoute.CenterLogin) },
                    onNavigateLogin = { viewModel.navigateTo(ScreenRoute.CenterLogin) }
                )
            }

            is ScreenRoute.FarmerDashboard -> {
                FarmerDashboardScreen(
                    viewModel = viewModel,
                    onNavigateMyCans = { viewModel.navigateTo(ScreenRoute.MyCans) },
                    onNavigateConnectCan = { viewModel.navigateTo(ScreenRoute.ConnectCan) },
                    onNavigateLiveCan = { canId ->
                        viewModel.selectCan(canId)
                        viewModel.navigateTo(ScreenRoute.LiveCanData)
                    },
                    onNavigateRecords = { viewModel.navigateTo(ScreenRoute.RecordsHistory) },
                    onNavigatePassport = { viewModel.navigateTo(ScreenRoute.ThermalPassport) },
                    onNavigateAlerts = { viewModel.navigateTo(ScreenRoute.AlertsList) },
                    onNavigateProfile = { viewModel.navigateTo(ScreenRoute.FarmerProfile) }
                )
            }

            is ScreenRoute.MyCans -> {
                MyCansScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(ScreenRoute.FarmerDashboard) },
                    onNavigateConnectCan = { viewModel.navigateTo(ScreenRoute.ConnectCan) },
                    onSelectCan = { canId ->
                        viewModel.selectCan(canId)
                        viewModel.navigateTo(ScreenRoute.LiveCanData)
                    }
                )
            }

            is ScreenRoute.ConnectCan -> {
                ConnectCanScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(ScreenRoute.MyCans) },
                    onConnectedNavigateToLive = {
                        viewModel.navigateTo(ScreenRoute.LiveCanData)
                    }
                )
            }

            is ScreenRoute.LiveCanData -> {
                val canId = selectedCanId ?: cans.firstOrNull()?.canId ?: "CAN001"
                LiveCanDataScreen(
                    canId = canId,
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(ScreenRoute.MyCans) },
                    onNavigatePassport = { viewModel.navigateTo(ScreenRoute.ThermalPassport) }
                )
            }

            is ScreenRoute.RecordsHistory -> {
                RecordsHistoryScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(ScreenRoute.FarmerDashboard) }
                )
            }

            is ScreenRoute.ThermalPassport -> {
                ThermalPassportScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(ScreenRoute.FarmerDashboard) }
                )
            }

            is ScreenRoute.AlertsList -> {
                AlertsListScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(ScreenRoute.FarmerDashboard) }
                )
            }

            is ScreenRoute.FarmerProfile -> {
                FarmerProfileScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(ScreenRoute.FarmerDashboard) },
                    onNavigateMyCans = { viewModel.navigateTo(ScreenRoute.MyCans) }
                )
            }

            is ScreenRoute.CenterDashboard -> {
                CenterDashboardScreen(
                    viewModel = viewModel,
                    onNavigateCanDetails = { canId ->
                        viewModel.selectCan(canId)
                        viewModel.navigateTo(ScreenRoute.ThermalPassport)
                    }
                )
            }

            // Dedicated Demo Routes
            is ScreenRoute.DemoFarmerDashboard -> {
                DemoFarmerDashboardScreen(
                    viewModel = viewModel,
                    onNavigateMyCans = { viewModel.navigateTo(ScreenRoute.DemoConnectCan) },
                    onNavigateConnect = { viewModel.navigateTo(ScreenRoute.DemoConnectCan) },
                    onNavigateLiveCan = { viewModel.navigateTo(ScreenRoute.DemoLiveDashboard) },
                    onNavigatePassport = { viewModel.navigateTo(ScreenRoute.DemoLiveDashboard) },
                    onNavigateMl = { viewModel.navigateTo(ScreenRoute.MilkQualityMl) },
                    onExitDemo = { viewModel.exitDemo() }
                )
            }

            is ScreenRoute.DemoConnectCan -> {
                DemoConnectCanScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(ScreenRoute.DemoFarmerDashboard) },
                    onConnectedNavigateToLive = { viewModel.navigateTo(ScreenRoute.DemoLiveDashboard) }
                )
            }

            is ScreenRoute.DemoLiveDashboard -> {
                DemoLiveDashboardScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(ScreenRoute.DemoFarmerDashboard) },
                    onNavigateMl = { viewModel.navigateTo(ScreenRoute.MilkQualityMl) }
                )
            }

            is ScreenRoute.DemoCentreDashboard -> {
                DemoCentreDashboardScreen(
                    viewModel = viewModel,
                    onNavigateLiveCan = { viewModel.navigateTo(ScreenRoute.DemoLiveDashboard) },
                    onNavigateMl = { viewModel.navigateTo(ScreenRoute.MilkQualityMl) },
                    onExitDemo = { viewModel.exitDemo() }
                )
            }

            is ScreenRoute.MilkQualityMl -> {
                val temp = if (viewModel.demoManager.isDemoActive.value) {
                    viewModel.demoManager.currentTemperature.value
                } else {
                    cans.firstOrNull { it.canId == selectedCanId }?.lastTemperature
                }
                MilkQualityMlScreen(
                    currentCanTemp = temp,
                    isDemoMode = viewModel.demoManager.isDemoActive.value,
                    onBack = {
                        if (viewModel.demoManager.isDemoActive.value) {
                            viewModel.navigateTo(ScreenRoute.DemoLiveDashboard)
                        } else {
                            viewModel.navigateTo(ScreenRoute.FarmerDashboard)
                        }
                    }
                )
            }

            else -> {
                SplashScreen(
                    onGetStarted = { viewModel.navigateTo(ScreenRoute.RoleSelection) },
                    onStartDemo = { viewModel.startFarmerDemo() }
                )
            }
        }

        // Global Warning Alert Modal (Real flow)
        activeWarningAlert?.let { alert ->
            WarningAlertDialog(
                alert = alert,
                onDismiss = { viewModel.dismissWarningAlert() },
                onViewLiveData = {
                    viewModel.dismissWarningAlert()
                    viewModel.selectCan(alert.canId)
                    viewModel.navigateTo(ScreenRoute.LiveCanData)
                }
            )
        }

        // Demo Warning Alert Modal (Triggered in demo when temp reaches 8.2, 8.7, 9.1)
        demoWarningAlert?.let { alertItem ->
            val alertEntity = AlertEntity(
                id = alertItem.id,
                canId = alertItem.canId,
                alertType = "TEMPERATURE_HIGH",
                temperature = alertItem.temperature,
                message = alertItem.message,
                timestamp = alertItem.timestamp
            )
            WarningAlertDialog(
                alert = alertEntity,
                onDismiss = { viewModel.demoManager.dismissWarningAlert() },
                onViewLiveData = {
                    viewModel.demoManager.dismissWarningAlert()
                    viewModel.navigateTo(ScreenRoute.DemoLiveDashboard)
                }
            )
        }
    }
}
