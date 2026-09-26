package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.demo.DemoSessionManager
import com.example.ui.AppViewModel
import com.example.ui.components.LiveTemperatureChart
import com.example.ui.components.TemperatureStatusBadge
import com.example.ui.theme.TempSafe
import com.example.ui.theme.TempSafeBg
import com.example.ui.theme.TempWarning
import com.example.ui.theme.TempWarningBg
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoFarmerDashboardScreen(
    viewModel: AppViewModel,
    onNavigateMyCans: () -> Unit,
    onNavigateConnect: () -> Unit,
    onNavigateLiveCan: () -> Unit,
    onNavigatePassport: () -> Unit,
    onNavigateMl: () -> Unit,
    onExitDemo: () -> Unit
) {
    val demoManager = viewModel.demoManager
    val isConnected by demoManager.isConnected.collectAsState()
    val currentTemp by demoManager.currentTemperature.collectAsState()
    val currentBatt by demoManager.currentBattery.collectAsState()
    val isWarning = currentTemp > 8.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Demo Farmer Dashboard", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20), fontSize = 18.sp)
                        Text("Deterministic CAN00123 Session", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A665E))
                    }
                },
                actions = {
                    TextButton(
                        onClick = onExitDemo,
                        modifier = Modifier.testTag("exit_demo_button")
                    ) {
                        Text("Exit Demo", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Demo Mode Notice Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFE8F5E9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Science, contentDescription = null, tint = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Demo Mode: Isolated simulation following exact test sequence for CAN00123.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1B5E20)
                    )
                }
            }

            // Farmer Profile Greeting Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Good Day, Ramesh", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF1F2923))
                        Text("Demo Farmer Account  •  +91 9876543210", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5A665E))
                    }
                }
            }

            // Status Overview Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Demo Cans", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A665E))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("1 Active", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isWarning) TempWarningBg else TempSafeBg),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Chilling Status", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A665E))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isWarning) "Warning" else "Normal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isWarning) TempWarning else TempSafe
                        )
                    }
                }
            }

            // Quick Action Grid
            Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateMyCans() }
                        .testTag("demo_action_my_cans")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Inventory2, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("My Cans", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateConnect() }
                        .testTag("demo_action_connect_can")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.BluetoothSearching, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Connect Can", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigatePassport() }
                        .testTag("demo_action_passport")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF0277BD), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Thermal Passport", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateMl() }
                        .testTag("demo_action_ml")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = Color(0xFF6A1B9A), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("ML Quality", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Monitored Smart Can Section
            Text("Monitored Smart Can", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(if (isConnected) Color(0xFF2E7D32) else Color(0xFFFFA000), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CAN00123", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isConnected) Color(0xFFE8F5E9) else Color(0xFFFFF8E1)
                        ) {
                            Text(
                                text = if (isConnected) "CONNECTED" else "DISCOVERABLE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isConnected) Color(0xFF2E7D32) else Color(0xFFE65100),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isConnected) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Current Temp", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A665E))
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", currentTemp)} °C",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isWarning) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Battery Level", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A665E))
                                Text(
                                    text = "$currentBatt%",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2923)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onNavigateLiveCan,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("view_demo_live_button")
                        ) {
                            Icon(imageVector = Icons.Default.Timeline, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View Live Telemetry Stream", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = "Smart Can CAN00123 is ready to pair. Connect to begin live telemetry stream.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF5A665E)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onNavigateConnect,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("connect_demo_can_button")
                        ) {
                            Icon(imageVector = Icons.Default.Bluetooth, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connect CAN00123", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoConnectCanScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onConnectedNavigateToLive: () -> Unit
) {
    val demoManager = viewModel.demoManager
    val isScanning by demoManager.isScanning.collectAsState()
    val isDiscovered by demoManager.isDiscovered.collectAsState()
    val isConnecting by demoManager.isConnecting.collectAsState()
    val isConnected by demoManager.isConnected.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connect Smart Can (Demo)", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF1B5E20))
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // BLE Graphic Banner
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = "Bluetooth",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(54.dp)
                )
            }

            Text(
                text = "Bluetooth Low Energy Discovery",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )

            Text(
                text = "Discover and pair with SmartCan_CAN00123 to initiate live passive cooling telemetry.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5A665E),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Step 1: Scan Button
            if (!isDiscovered && !isConnecting && !isConnected) {
                Button(
                    onClick = { demoManager.startDemoScan() },
                    enabled = !isScanning,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("demo_scan_button")
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Scanning for Smart Cans...")
                    } else {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan for Smart Can", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            // Step 2: Discovered Device Card
            if (isDiscovered && !isConnected) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Inventory2, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("SmartCan_CAN00123", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("RSSI: -58 dBm  •  Ready", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A665E))
                                }
                            }

                            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFE8F5E9)) {
                                Text("Discovered", color = Color(0xFF2E7D32), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                demoManager.selectAndConnectDemoCan {
                                    onConnectedNavigateToLive()
                                }
                            },
                            enabled = !isConnecting,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("select_demo_device_button")
                        ) {
                            if (isConnecting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connecting...")
                            } else {
                                Text("Pair & Connect CAN00123", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Step 3: Connected State Card
            if (isConnected) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Connected to CAN00123!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                        Text("Telemetry stream is actively streaming.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5A665E))

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onConnectedNavigateToLive,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("open_live_dashboard_button")
                        ) {
                            Text("Open Live Dashboard", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoLiveDashboardScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onNavigateMl: () -> Unit
) {
    val demoManager = viewModel.demoManager
    val currentTemp by demoManager.currentTemperature.collectAsState()
    val currentBatt by demoManager.currentBattery.collectAsState()
    val telemetryHistory by demoManager.telemetryHistory.collectAsState()
    val alertsList by demoManager.alertsList.collectAsState()
    val activeWarningAlert by demoManager.activeWarningAlert.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Live Dashboard, 1: History, 2: Alerts, 3: Thermal Passport

    val isWarning = currentTemp > 8.0
    val tempStatus = if (isWarning) "Temperature Warning" else "Temperature Normal"

    val excursionCount = telemetryHistory.count { it.temperature > 8.0 }
    val timeAbove8Min = excursionCount * 2

    // Rule-based thermal risk using temperature and duration above 8°C (LOW, MODERATE, HIGH)
    val thermalRisk = when {
        currentTemp > 9.0 || timeAbove8Min >= 20 -> "HIGH"
        currentTemp > 8.0 || timeAbove8Min > 0 -> "MODERATE"
        else -> "LOW"
    }

    val riskColor = when (thermalRisk) {
        "HIGH" -> Color(0xFFD32F2F)
        "MODERATE" -> Color(0xFFF57F17)
        else -> Color(0xFF2E7D32)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("CAN00123", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFE8F5E9)) {
                            Text("CONNECTED", color = Color(0xFF2E7D32), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF1B5E20))
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateMl, modifier = Modifier.testTag("nav_ml_icon_button")) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = "ML", tint = Color(0xFF2E7D32))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Live") },
                    label = { Text("Live") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        BadgedBox(badge = {
                            if (alertsList.isNotEmpty()) {
                                Badge { Text("${alertsList.size}") }
                            }
                        }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Alerts")
                        }
                    },
                    label = { Text("Alerts") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.VerifiedUser, contentDescription = "Passport") },
                    label = { Text("Passport") }
                )
            }
        },
        containerColor = Color(0xFFF6F8F6)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> {
                    // Live Telemetry Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Warning Banner when temperature exceeds 8°C (8.2, 8.7, 9.1)
                        if (isWarning) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE57373))
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("demo_temperature_warning_card")
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFD32F2F), modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Temperature Warning", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Temperature exceeded the recommended range. Milk quality may be at risk.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFB71C1C)
                                        )
                                    }
                                }
                            }
                        }

                        // Main Live Metric Card (Matches reference Screen 7 in image.png)
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Smart Can CAN00123", fontWeight = FontWeight.Bold, color = Color(0xFF5A665E))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isWarning) TempWarningBg else TempSafeBg
                                    ) {
                                        Text(
                                            text = tempStatus,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isWarning) TempWarning else TempSafe,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Current Temperature Display
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", currentTemp)} °C",
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isWarning) Color(0xFFD32F2F) else Color(0xFF1B5E20)
                                )

                                Text(
                                    text = if (isWarning) "Above Recommended Chilling Threshold (8°C)" else "Within Safe Chilling Zone (4°C - 8°C)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isWarning) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Divider(color = Color(0xFFF1F5F2))

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Battery", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A665E))
                                        Text("$currentBatt%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Thermal Risk", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A665E))
                                        Text(thermalRisk, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = riskColor)
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Status", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A665E))
                                        Text("Active", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                    }
                                }
                            }
                        }

                        // Live Temperature Telemetry Chart
                        val temps = telemetryHistory.map { it.temperature }
                        LiveTemperatureChart(temperatures = temps)

                        // ML Quality Analysis Entry Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateMl() }
                                .testTag("open_ml_analysis_card")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = Color(0xFF6A1B9A), modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Milk Quality ML Analysis", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        Text("Input 7 verified composition features", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A665E))
                                    }
                                }
                                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF8B968F))
                            }
                        }
                    }
                }

                1 -> {
                    // History Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text("Demo Telemetry Readings (${telemetryHistory.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                        }

                        items(telemetryHistory.reversed(), key = { it.index }) { point ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                            text = "${String.format(Locale.US, "%.1f", point.temperature)} °C",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (point.isWarning) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                                        )
                                        Text("Battery: ${point.battery}%", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5A665E))
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        TemperatureStatusBadge(isSafe = !point.isWarning)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(point.timeLabel, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B968F))
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Alerts Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("Temperature Excursion Alerts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                        }

                        if (alertsList.isEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(48.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("No Alerts Recorded", fontWeight = FontWeight.Bold)
                                        Text("Cold-chain has maintained 4°C - 8°C.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5A665E))
                                    }
                                }
                            }
                        } else {
                            items(alertsList, key = { it.id }) { alert ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("TEMPERATURE WARNING", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F), fontSize = 12.sp)
                                            Text("${String.format(Locale.US, "%.1f", alert.temperature)} °C", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(alert.message, style = MaterialTheme.typography.bodySmall, color = Color(0xFFB71C1C))
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // Thermal Passport Tab for Demo Session
                    val temps = telemetryHistory.map { it.temperature }
                    val minTemp = temps.minOrNull() ?: 5.2
                    val maxTemp = temps.maxOrNull() ?: currentTemp
                    val avgTemp = if (temps.isNotEmpty()) temps.average() else 5.2
                    val warningCount = telemetryHistory.count { it.isWarning }
                    val timeAbove8 = warningCount * 2 // 2 minutes per sample point

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("THERMAL PASSPORT", fontWeight = FontWeight.ExtraBold, color = Color(0xFF1B5E20), letterSpacing = 1.sp)
                                        Text("Cold-Chain Integrity Certificate", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A665E))
                                    }
                                    Icon(imageVector = Icons.Default.Verified, contentDescription = "Certified", tint = Color(0xFF2E7D32), modifier = Modifier.size(32.dp))
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = Color(0xFFE8F5E9))
                                Spacer(modifier = Modifier.height(14.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Smart Can ID:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5A665E))
                                    Text("CAN00123", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Registered Farmer:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5A665E))
                                    Text("Ramesh Kumar (Demo)", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Date of Batch:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5A665E))
                                    Text(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.US)), fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Temperature Range:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5A665E))
                                    Text("${String.format(Locale.US, "%.1f", minTemp)}°C - ${String.format(Locale.US, "%.1f", maxTemp)}°C (Avg: ${String.format(Locale.US, "%.1f", avgTemp)}°C)", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Duration Above 8°C:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5A665E))
                                    Text("$timeAbove8 mins", fontWeight = FontWeight.Bold, color = if (timeAbove8 > 0) Color(0xFFD32F2F) else Color(0xFF2E7D32))
                                }
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Warning Alerts Logged:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5A665E))
                                    Text("$warningCount events", fontWeight = FontWeight.Bold, color = if (warningCount > 0) Color(0xFFD32F2F) else Color(0xFF2E7D32))
                                }
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Thermal Risk Level:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5A665E))
                                    Text(thermalRisk, fontWeight = FontWeight.Bold, color = riskColor)
                                }
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("ML Quality Prediction:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5A665E))
                                    Text("Verified by Model", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (warningCount == 0) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (warningCount == 0) "CERTIFIED: Cold-Chain Integrity Maintained (4°C - 8°C)" else "NOTICE: Minor Thermal Deviation Recorded during Transport",
                                        fontWeight = FontWeight.Bold,
                                        color = if (warningCount == 0) Color(0xFF1B5E20) else Color(0xFFE65100),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(12.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoCentreDashboardScreen(
    viewModel: AppViewModel,
    onNavigateLiveCan: () -> Unit,
    onNavigateMl: () -> Unit,
    onExitDemo: () -> Unit
) {
    val demoManager = viewModel.demoManager
    val currentTemp by demoManager.currentTemperature.collectAsState()
    val isWarning = currentTemp > 8.0

    // Ensure telemetry stream is active
    LaunchedEffect(Unit) {
        demoManager.directConnectDemoCan()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Demo Collection Centre", fontWeight = FontWeight.Bold, color = Color(0xFF01579B), fontSize = 18.sp)
                        Text("Central Cooperative Dairy Portal", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A665E))
                    }
                },
                actions = {
                    TextButton(onClick = onExitDemo) {
                        Text("Exit Demo", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFE1F5FE),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, tint = Color(0xFF0277BD))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Demo Centre receiving synchronized cloud telemetry for CAN00123.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF01579B)
                    )
                }
            }

            // Overview Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Farmers", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A665E))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("1 Active", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF0277BD))
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isWarning) TempWarningBg else TempSafeBg),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Thermal Status", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A665E))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isWarning) "Excursion" else "Safe",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isWarning) TempWarning else TempSafe
                        )
                    }
                }
            }

            Text("Monitored Farmer Smart Cans", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF01579B))

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().clickable { onNavigateLiveCan() }
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("CAN00123", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Farmer: Ramesh Kumar", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5A665E))
                        }

                        TemperatureStatusBadge(isSafe = !isWarning)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Current Chilling Temp", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A665E))
                            Text(
                                text = "${String.format(Locale.US, "%.1f", currentTemp)} °C",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isWarning) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                            )
                        }

                        Button(
                            onClick = onNavigateLiveCan,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("View Passport & Live", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
