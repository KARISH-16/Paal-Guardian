package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CanEntity
import com.example.ui.AppViewModel
import com.example.ui.components.TemperatureStatusBadge
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCansScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onNavigateConnectCan: () -> Unit,
    onSelectCan: (String) -> Unit
) {
    val cans by viewModel.allCans.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Cans", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20)) },
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Connect New Can Button (Matching Screen 5 in image.png)
            Button(
                onClick = onNavigateConnectCan,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("my_cans_connect_new_can_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Connect New Can", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (cans.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = "No Cans",
                            tint = Color(0xFF8B968F),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Smart Can connected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2923)
                        )
                        Text(
                            text = "Tap '+ Connect New Can' to scan and pair your Smart Can.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF5A665E),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(cans, key = { it.canId }) { can ->
                        CanListItemCard(
                            can = can,
                            onClick = {
                                viewModel.selectCan(can.canId)
                                onSelectCan(can.canId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CanListItemCard(
    can: CanEntity,
    onClick: () -> Unit
) {
    val isSafe = (can.lastTemperature ?: 5.0) in 4.0..8.0
    val isConnected = can.isConnected

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("can_item_${can.canId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Sensors,
                contentDescription = "Can",
                tint = if (isConnected) Color(0xFF2E7D32) else Color(0xFF8B968F),
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = can.canId,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2923)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isConnected) "• Connected" else "• Offline",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isConnected) Color(0xFF2E7D32) else Color(0xFF8B968F),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (can.lastTemperature != null) {
                        Text(
                            text = "Temp: ${String.format(Locale.US, "%.1f", can.lastTemperature)} °C",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSafe) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "No readings yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8B968F)
                        )
                    }

                    if (can.lastBattery != null) {
                        Text(
                            text = "Battery: ${can.lastBattery}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF5A665E)
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Details",
                tint = Color(0xFF8B968F)
            )
        }
    }
}
