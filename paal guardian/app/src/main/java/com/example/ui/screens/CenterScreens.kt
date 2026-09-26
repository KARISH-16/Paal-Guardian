package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CanEntity
import com.example.ui.AppViewModel
import com.example.ui.components.TemperatureStatusBadge
import com.example.ui.theme.TempSafe
import com.example.ui.theme.TempSafeBg
import com.example.ui.theme.TempWarning
import com.example.ui.theme.TempWarningBg
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenterLoginScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onNavigateRegister: () -> Unit
) {
    val authState by viewModel.authUiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF01579B))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF6F8F6))
            )
        },
        containerColor = Color(0xFFF6F8F6)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE1F5FE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = "Center",
                    tint = Color(0xFF0277BD),
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Collection Center Login",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF01579B)
            )

            Text(
                text = "Dairy cooperative & chilling plant portal",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF5A665E),
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            OutlinedTextField(
                value = authState.identifier,
                onValueChange = { viewModel.updateAuthIdentifier(it) },
                label = { Text("Center Email / Code") },
                placeholder = { Text("center@dairyfed.org") },
                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("center_identifier_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = authState.password,
                onValueChange = { viewModel.updateAuthPassword(it) },
                label = { Text("Password") },
                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("center_password_input")
            )

            if (authState.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = authState.errorMessage ?: "",
                    color = Color(0xFFD32F2F),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.login("collection_center") },
                enabled = !authState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("center_login_button")
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Login to Center", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Separate Demo Login Entry for Center
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f), color = Color(0xFFD6DDD8))
                Text(
                    text = "  OR DEMO MODE  ",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8B968F),
                    fontWeight = FontWeight.Bold
                )
                Divider(modifier = Modifier.weight(1f), color = Color(0xFFD6DDD8))
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = { viewModel.startCenterDemo() },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0277BD)),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF0277BD))
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("center_demo_login_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = "Demo",
                    tint = Color(0xFF0277BD)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Demo Login",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("New Center Portal?", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF5A665E))
                TextButton(onClick = onNavigateRegister) {
                    Text("Register", fontWeight = FontWeight.Bold, color = Color(0xFF0277BD))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenterRegisterScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onNavigateLogin: () -> Unit
) {
    val authState by viewModel.authUiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register Collection Center", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF6F8F6))
            )
        },
        containerColor = Color(0xFFF6F8F6)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = authState.name,
                onValueChange = { viewModel.updateAuthName(it) },
                label = { Text("Center Name (e.g. Mettupalayam Milk Center)") },
                leadingIcon = { Icon(imageVector = Icons.Default.Business, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = authState.phone,
                onValueChange = { viewModel.updateAuthPhone(it) },
                label = { Text("Center Contact Phone") },
                leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = authState.identifier,
                onValueChange = { viewModel.updateAuthIdentifier(it) },
                label = { Text("Center Admin Email") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = authState.password,
                onValueChange = { viewModel.updateAuthPassword(it) },
                label = { Text("Password") },
                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (authState.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = authState.errorMessage ?: "",
                    color = Color(0xFFD32F2F),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.register("collection_center") },
                enabled = !authState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Create Center Account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already registered?", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF5A665E))
                TextButton(onClick = onNavigateLogin) {
                    Text("Login here", fontWeight = FontWeight.Bold, color = Color(0xFF0277BD))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenterDashboardScreen(
    viewModel: AppViewModel,
    onNavigateCanDetails: (String) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val cans by viewModel.allCans.collectAsState()
    val alerts by viewModel.allAlerts.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Overview, 1: Farmers, 2: All Cans, 3: Analytics, 4: Profile

    val centerName = currentUser?.name?.ifEmpty { "Mettupalayam Milk Center" } ?: "Mettupalayam Milk Center"
    val warningCans = cans.filter { it.status == "Warning" }
    val safeCans = cans.filter { it.status != "Warning" }
    val totalCans = cans.size.coerceAtLeast(1)
    val compliancePercent = (safeCans.size * 100) / totalCans

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE1F5FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = Color(0xFF0277BD),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = centerName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF01579B)
                            )
                            Text(
                                text = "Central Dairy Chilling Node",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF5A665E)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.syncNow() }) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = "Sync", tint = Color(0xFF0277BD))
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
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Overview") },
                    label = { Text("Overview") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.People, contentDescription = "Farmers") },
                    label = { Text("Farmers") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "All Cans") },
                    label = { Text("All Cans") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Analytics") },
                    label = { Text("Analytics") }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                    label = { Text("Profile") }
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
                0 -> CenterOverviewTab(
                    cans = cans,
                    warnings = warningCans.size,
                    compliance = compliancePercent,
                    onNavigateCan = onNavigateCanDetails
                )
                1 -> CenterFarmersTab(viewModel = viewModel)
                2 -> CenterAllCansTab(cans = cans, onNavigateCan = onNavigateCanDetails)
                3 -> CenterAnalyticsTab(compliance = compliancePercent, totalCans = cans.size, warningCount = warningCans.size)
                4 -> CenterProfileTab(viewModel = viewModel, centerName = centerName)
            }
        }
    }
}

@Composable
fun CenterOverviewTab(
    cans: List<CanEntity>,
    warnings: Int,
    compliance: Int,
    onNavigateCan: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
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
                        Text("Active Cans", style = MaterialTheme.typography.labelMedium, color = Color(0xFF5A665E))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${cans.size}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF0277BD))
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (warnings > 0) TempWarningBg else TempSafeBg),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Cold Warnings", style = MaterialTheme.typography.labelMedium, color = Color(0xFF5A665E))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$warnings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = if (warnings > 0) TempWarning else TempSafe)
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Compliance", style = MaterialTheme.typography.labelMedium, color = Color(0xFF5A665E))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$compliance%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }
            }
        }

        item {
            Text(
                text = "Monitored Farmer Milk Cans",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF01579B),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (cans.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No active cans synchronized to this center yet.", color = Color(0xFF8B968F))
                    }
                }
            }
        } else {
            items(cans, key = { it.canId }) { can ->
                val isSafe = (can.lastTemperature ?: 5.0) in 4.0..8.0
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateCan(can.canId) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(can.canId, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("• ${can.farmerName}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5A665E))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (can.lastTemperature != null)
                                    "Temp: ${String.format(Locale.US, "%.1f", can.lastTemperature)} °C"
                                else
                                    "Awaiting reading",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSafe) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                            )
                        }

                        TemperatureStatusBadge(isSafe = isSafe)
                    }
                }
            }
        }
    }
}

@Composable
fun CenterFarmersTab(viewModel: AppViewModel) {
    val farmers by viewModel.getAllFarmers().collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Registered Farmers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF01579B))
        }

        if (farmers.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.People, contentDescription = null, tint = Color(0xFF8B968F), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No farmers registered yet", fontWeight = FontWeight.Bold)
                        Text("Farmer profiles synced from cloud will display here.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5A665E))
                    }
                }
            }
        } else {
            items(farmers, key = { it.id }) { farmer ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color(0xFF0277BD), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(farmer.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(farmer.phone ?: farmer.email, style = MaterialTheme.typography.bodySmall, color = Color(0xFF5A665E))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CenterAllCansTab(
    cans: List<CanEntity>,
    onNavigateCan: (String) -> Unit
) {
    var filterSafeOnly by remember { mutableStateOf<Boolean?>(null) } // null = all, true = safe, false = warning

    val filtered = when (filterSafeOnly) {
        true -> cans.filter { (it.lastTemperature ?: 5.0) in 4.0..8.0 }
        false -> cans.filter { (it.lastTemperature ?: 5.0) !in 4.0..8.0 }
        null -> cans
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterSafeOnly == null,
                onClick = { filterSafeOnly = null },
                label = { Text("All (${cans.size})") }
            )
            FilterChip(
                selected = filterSafeOnly == true,
                onClick = { filterSafeOnly = true },
                label = { Text("Safe 4-8°C") }
            )
            FilterChip(
                selected = filterSafeOnly == false,
                onClick = { filterSafeOnly = false },
                label = { Text("Excursion >8°C") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered, key = { it.canId }) { can ->
                val isSafe = (can.lastTemperature ?: 5.0) in 4.0..8.0
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateCan(can.canId) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(can.canId, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Farmer: ${can.farmerName}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5A665E))
                            if (can.lastTemperature != null) {
                                Text(
                                    "${String.format(Locale.US, "%.1f", can.lastTemperature)} °C",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSafe) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                )
                            }
                        }

                        TemperatureStatusBadge(isSafe = isSafe)
                    }
                }
            }
        }
    }
}

@Composable
fun CenterAnalyticsTab(compliance: Int, totalCans: Int, warningCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Cold-Chain Compliance Rate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF01579B))
                Spacer(modifier = Modifier.height(14.dp))
                LinearProgressIndicator(
                    progress = { (compliance / 100f).coerceIn(0f, 1f) },
                    color = Color(0xFF2E7D32),
                    trackColor = Color(0xFFE8F5E9),
                    modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("$compliance% compliance with 4°C - 8°C milk chilling standards.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5A665E))
            }
        }

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Temperature Distribution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF01579B))
                Spacer(modifier = Modifier.height(10.dp))
                Text("• Normal / Safe Range: ${totalCans - warningCount} cans", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF2E7D32))
                Spacer(modifier = Modifier.height(6.dp))
                Text("• Thermal Excursion Warning: $warningCount cans", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFD32F2F))
            }
        }
    }
}

@Composable
fun CenterProfileTab(
    viewModel: AppViewModel,
    centerName: String
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(centerName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF01579B))
                Text("Collection Center Station", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF5A665E))
            }
        }

        Button(
            onClick = { showLogoutDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Log Out of Center", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out") },
            text = { Text("Are you sure you want to log out of the Collection Center portal?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
