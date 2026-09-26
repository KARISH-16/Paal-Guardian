package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ble.BleConnectionState
import com.example.data.local.entity.CanEntity
import com.example.ui.AppViewModel
import com.example.ui.ScreenRoute
import com.example.ui.components.TemperatureStatusBadge
import com.example.ui.theme.TempSafe
import com.example.ui.theme.TempSafeBg
import com.example.ui.theme.TempWarning
import com.example.ui.theme.TempWarningBg
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerDashboardScreen(
    viewModel: AppViewModel,
    onNavigateMyCans: () -> Unit,
    onNavigateConnectCan: () -> Unit,
    onNavigateLiveCan: (String) -> Unit,
    onNavigateRecords: () -> Unit,
    onNavigatePassport: () -> Unit,
    onNavigateAlerts: () -> Unit,
    onNavigateProfile: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val cans by viewModel.allCans.collectAsState()
    val alerts by viewModel.allAlerts.collectAsState()
    val bleState by viewModel.bleConnectionState.collectAsState()
    val connectedCanId by viewModel.connectedCanId.collectAsState()

    val farmerName = currentUser?.name?.ifEmpty { "Ramesh" } ?: "Ramesh"
    val activeCansCount = cans.size
    val hasWarning = cans.any { it.status == "Warning" }
    val unreadAlerts = alerts.filter { !it.isAcknowledged }.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = "Leaf",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Good Day, $farmerName",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                            Text(
                                text = "Keep your milk safe!",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF5A665E)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateAlerts, modifier = Modifier.testTag("dashboard_notifications_icon")) {
                        BadgedBox(
                            badge = {
                                if (unreadAlerts > 0) {
                                    Badge(containerColor = Color(0xFFD32F2F)) {
                                        Text("$unreadAlerts")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alerts",
                                tint = Color(0xFF1B5E20)
                            )
                        }
                    }
                    IconButton(onClick = onNavigateProfile, modifier = Modifier.testTag("dashboard_profile_icon")) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = Color(0xFF1B5E20)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF6F8F6)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Summary Cards Row (matching screen 4 in image.png)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 1: My Cans
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateMyCans() }
                            .testTag("dashboard_my_cans_stat_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "My Cans",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF5A665E)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$activeCansCount",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (bleState == BleConnectionState.SUBSCRIBED) "1 Connected" else "0 Connected",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (bleState == BleConnectionState.SUBSCRIBED) Color(0xFF2E7D32) else Color(0xFF757575)
                            )
                        }
                    }

                    // Card 2: Temperature Condition
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (hasWarning) TempWarningBg else TempSafeBg
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (hasWarning) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (hasWarning) TempWarning else TempSafe,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (hasWarning) "Warning" else "All Cans Normal",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasWarning) TempWarning else TempSafe
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (hasWarning)
                                    "Milk temperature exceeded 8°C"
                                else
                                    "Milk is within safe temperature (4°C - 8°C)",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (hasWarning) TempWarning else TempSafe,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Quick Actions Section (matching screen 4 Quick Actions in image.png)
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20),
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionTile(
                        icon = Icons.Default.Inventory2,
                        label = "View My\nCans",
                        onClick = onNavigateMyCans,
                        modifier = Modifier.weight(1f).testTag("quick_action_my_cans")
                    )
                    QuickActionTile(
                        icon = Icons.Default.History,
                        label = "Milk\nRecords",
                        onClick = onNavigateRecords,
                        modifier = Modifier.weight(1f).testTag("quick_action_records")
                    )
                    QuickActionTile(
                        icon = Icons.Default.Verified,
                        label = "Thermal\nPassport",
                        onClick = onNavigatePassport,
                        modifier = Modifier.weight(1f).testTag("quick_action_passport")
                    )
                    QuickActionTile(
                        icon = Icons.Default.Notifications,
                        label = "Alerts\nLog",
                        badgeCount = unreadAlerts,
                        onClick = onNavigateAlerts,
                        modifier = Modifier.weight(1f).testTag("quick_action_alerts")
                    )
                }
            }

            // Active Smart Can Live Connection Card
            item {
                Text(
                    text = "Active Can Monitoring",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                val activeCan = cans.firstOrNull { it.isConnected } ?: cans.firstOrNull()

                if (activeCan != null && activeCan.lastTemperature != null) {
                    val isSafe = activeCan.lastTemperature in 4.0..8.0
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateLiveCan(activeCan.canId) }
                            .testTag("dashboard_active_can_card")
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Sensors,
                                        contentDescription = "Can",
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = activeCan.canId,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1F2923)
                                    )
                                }

                                TemperatureStatusBadge(isSafe = isSafe)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "Current Milk Temp",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFF5A665E)
                                    )
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", activeCan.lastTemperature)} °C",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSafe) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.BatteryChargingFull,
                                            contentDescription = "Battery",
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${activeCan.lastBattery ?: 100}%",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text(
                                        text = "Tap for Live Details",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Empty state: "No Smart Can connected" (strict prompt requirement: NEVER show fake numbers!)
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_no_can_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BluetoothSearching,
                                    contentDescription = "No Smart Can",
                                    tint = Color(0xFF8B968F),
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "No Smart Can connected",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2923)
                            )

                            Text(
                                text = "Pair with your Smart Passive Chilling Can via Bluetooth to stream real-time temperature telemetry.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5A665E),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = onNavigateConnectCan,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("connect_can_dashboard_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bluetooth,
                                    contentDescription = "Bluetooth",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Connect Smart Can")
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun QuickActionTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    badgeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge(containerColor = Color(0xFFD32F2F)) {
                            Text("$badgeCount")
                        }
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2923),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}
