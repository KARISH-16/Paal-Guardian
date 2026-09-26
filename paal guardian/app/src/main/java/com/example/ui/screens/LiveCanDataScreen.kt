package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ble.BleConnectionState
import com.example.data.local.entity.SensorReadingEntity
import com.example.ui.AppViewModel
import com.example.ui.components.LabParameterDialog
import com.example.ui.components.TemperatureStatusBadge
import com.example.ui.components.ThermalRiskBadge
import com.example.ui.theme.TempSafe
import com.example.ui.theme.TempSafeBg
import com.example.ui.theme.TempWarning
import com.example.ui.theme.TempWarningBg
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveCanDataScreen(
    canId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onNavigatePassport: () -> Unit
) {
    val can by viewModel.getCanById(canId).collectAsState(initial = null)
    val latestReading by viewModel.getLatestReadingForCan(canId).collectAsState(initial = null)
    val recentReadings by viewModel.getReadingsForCan(canId).collectAsState(initial = emptyList())
    val bleState by viewModel.bleConnectionState.collectAsState()
    val mlResult by viewModel.mlInferenceResult.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Live, 1: History, 2: ML, 3: Settings
    var showLabDialog by remember { mutableStateOf(false) }

    val isDeviceConnected = can?.isConnected == true && bleState == BleConnectionState.SUBSCRIBED
    val currentTemp = latestReading?.temperature ?: can?.lastTemperature
    val isSafe = currentTemp != null && currentTemp in 4.0..8.0
    val battery = latestReading?.battery ?: can?.lastBattery

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = canId,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        // Connected / Disconnected status pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDeviceConnected) Color(0xFFE8F5E9) else Color(0xFFF1F5F2))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isDeviceConnected) "Connected" else "Offline",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDeviceConnected) Color(0xFF2E7D32) else Color(0xFF757575)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF1B5E20))
                    }
                },
                actions = {
                    if (battery != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BatteryChargingFull,
                                contentDescription = "Battery",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$battery%",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color(0xFF1F2923)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF6F8F6)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row (Matching screen 7 tabs: Live Data, History, ML, Settings)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF2E7D32)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Live Data", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("tab_live_data")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("History", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("tab_history")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("ML Quality", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("tab_ml")
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("tab_settings")
                )
            }

            when (selectedTab) {
                0 -> LiveDataTabContent(
                    canId = canId,
                    isDeviceConnected = isDeviceConnected,
                    currentTemp = currentTemp,
                    isSafe = isSafe,
                    battery = battery,
                    lastSeen = latestReading?.timestamp ?: can?.lastSeenTimestamp,
                    onNavigatePassport = onNavigatePassport
                )
                1 -> HistoryTabContent(
                    readings = recentReadings
                )
                2 -> MlTabContent(
                    canId = canId,
                    currentTemp = currentTemp ?: 5.0,
                    mlResult = mlResult,
                    onOpenLabDialog = { showLabDialog = true }
                )
                3 -> SettingsTabContent(
                    canId = canId,
                    isDeviceConnected = isDeviceConnected,
                    onDisconnect = { viewModel.disconnectCan() }
                )
            }
        }

        if (showLabDialog) {
            LabParameterDialog(
                canId = canId,
                currentTemperature = currentTemp ?: 5.0,
                onDismiss = { showLabDialog = false },
                onSubmit = { inputs ->
                    viewModel.evaluateMl(canId, inputs)
                }
            )
        }
    }
}

@Composable
fun LiveDataTabContent(
    canId: String,
    isDeviceConnected: Boolean,
    currentTemp: Double?,
    isSafe: Boolean,
    battery: Int?,
    lastSeen: String?,
    onNavigatePassport: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Main Temperature Card (Matching screen 7 in image.png)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("live_temp_display_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Milk Temperature",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5A665E)
                        )

                        if (currentTemp != null) {
                            TemperatureStatusBadge(isSafe = isSafe)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (currentTemp != null) {
                        Text(
                            text = "${String.format(Locale.US, "%.1f", currentTemp)} °C",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSafe) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                        )
                    } else {
                        // Empty state: explicitly state no connection, never fake!
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "-- °C",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8B968F)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No Smart Can connected",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Battery and Last Sync Stats
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF6F8F6))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BatteryChargingFull,
                                contentDescription = "Battery",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Battery", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A665E))
                                Text(
                                    text = if (battery != null) "$battery%" else "--",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Sync",
                                tint = Color(0xFF0288D1),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Last Telemetry", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A665E))
                                Text(
                                    text = lastSeen?.take(16)?.replace("T", " ") ?: "Awaiting sync",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Milk Status Banner (Matching screen 7 green/red banner in image.png)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSafe) TempSafeBg else TempWarningBg
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isSafe) Color(0xFFC8E6C9) else Color(0xFFFFCDD2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSafe) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isSafe) TempSafe else TempWarning,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = if (isSafe) "Milk Status: Safe" else "Warning: Temperature Exceeded",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSafe) TempSafe else TempWarning
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isSafe)
                                "Within Safe Range (4°C - 8°C). Keep it chilled!"
                            else
                                "Temperature exceeded the recommended range. Milk quality may be at risk.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSafe) Color(0xFF1B5E20) else Color(0xFFB71C1C),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Rule-Based Thermal Risk Status
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Thermal Risk (Rule-Based)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                        ThermalRiskBadge(riskLevel = if (isSafe) "LOW" else "MODERATE")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Real-time thermal risk is continuously tracked from temperature and duration above 8°C.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF5A665E)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = onNavigatePassport,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("view_thermal_passport_button")
                    ) {
                        Icon(imageVector = Icons.Default.Verified, contentDescription = "Passport")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View Full Thermal Passport")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun HistoryTabContent(
    readings: List<SensorReadingEntity>
) {
    if (readings.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "No history",
                    tint = Color(0xFF8B968F),
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Readings Stored",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2923)
                )
                Text(
                    text = "Sensor telemetry received from the Smart Can over BLE will be stored in SQLite and listed here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF5A665E),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(readings, key = { it.id }) { reading ->
                val safe = reading.temperature in 4.0..8.0
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = reading.timestamp.take(19).replace("T", " "),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF5A665E)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${String.format(Locale.US, "%.1f", reading.temperature)} °C",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (safe) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Battery: ${reading.battery}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5A665E),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            TemperatureStatusBadge(isSafe = safe)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MlTabContent(
    canId: String,
    currentTemp: Double,
    mlResult: com.example.ml.MlInferenceResult?,
    onOpenLabDialog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Milk Quality ML Inference",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "ML",
                        tint = Color(0xFF2E7D32)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (mlResult != null && mlResult.isAvailable) {
                    // Real ML Prediction Output
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (mlResult.prediction) {
                            "Good" -> TempSafeBg
                            "Warning" -> Color(0xFFFFF8E1)
                            else -> TempWarningBg
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Predicted Quality: ${mlResult.prediction}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = when (mlResult.prediction) {
                                    "Good" -> TempSafe
                                    "Warning" -> Color(0xFFF57F17)
                                    else -> TempWarning
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Model Confidence: ${String.format(Locale.US, "%.1f", mlResult.confidence)}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Evaluated at: ${mlResult.timestamp}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF5A665E)
                            )
                        }
                    }
                } else {
                    // Strict prompt requirement:
                    // "ML prediction unavailable"
                    // "Required sensor inputs are not available."
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF3E0),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "ML prediction unavailable",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Required sensor inputs are not available.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFBF360C)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Current Smart Can provides only real temperature, battery and timestamp.\nFeatures required for Random Forest model:\ntemperature_c, ph, fat_percent, protein_percent, turbidity_ntu, density_g_ml, storage_duration_hours.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5A665E),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onOpenLabDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("enter_lab_parameters_button")
                ) {
                    Icon(imageVector = Icons.Default.Science, contentDescription = "Lab")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Input Verified Lab Parameters", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SettingsTabContent(
    canId: String,
    isDeviceConnected: Boolean,
    onDisconnect: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Can Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Can Identifier: $canId", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Text("BLE Service: 0000A001-0000-1000-8000-00805F9B34FB", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5A665E))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Characteristic: 0000A002-0000-1000-8000-00805F9B34FB", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5A665E))
                Spacer(modifier = Modifier.height(16.dp))

                if (isDeviceConnected) {
                    Button(
                        onClick = onDisconnect,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("disconnect_can_button")
                    ) {
                        Text("Disconnect Can", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
