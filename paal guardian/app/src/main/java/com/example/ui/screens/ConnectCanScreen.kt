package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ble.BleConnectionState
import com.example.ble.DiscoveredCanDevice
import com.example.ui.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectCanScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onConnectedNavigateToLive: (String) -> Unit
) {
    val context = LocalContext.current
    val bleState by viewModel.bleConnectionState.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val connectedCanId by viewModel.connectedCanId.collectAsState()

    var permissionsGranted by remember { mutableStateOf(false) }

    val requiredPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionsGranted = results.values.all { it }
        if (permissionsGranted) {
            viewModel.startBleScan()
        }
    }

    LaunchedEffect(connectedCanId, bleState) {
        if (bleState == BleConnectionState.SUBSCRIBED && connectedCanId != null) {
            onConnectedNavigateToLive(connectedCanId!!)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connect New Can", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20)) },
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
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Bluetooth Graphic Circle (Matching screen 6 in image.png)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE1F5FE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = "Bluetooth",
                    tint = Color(0xFF0288D1),
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Turn on Bluetooth to connect your can",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )

            // Instruction list
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "1. Keep the smart can nearby (< 5 meters)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1F2923)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "2. Make sure the Smart Can is ON and broadcasting",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1F2923)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "3. Tap 'Scan for Cans' to search for device",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1F2923)
                    )
                }
            }

            // Scan Action Button
            val isScanning = bleState == BleConnectionState.SCANNING
            Button(
                onClick = {
                    if (!viewModel.repository.bleManager.isBluetoothEnabled) {
                        try {
                            context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                        } catch (_: Exception) {}
                    } else if (!permissionsGranted) {
                        permissionLauncher.launch(requiredPermissions)
                    } else {
                        if (isScanning) viewModel.stopBleScan() else viewModel.startBleScan()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isScanning) Color(0xFF5A665E) else Color(0xFF2E7D32)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("scan_for_cans_button")
            ) {
                if (isScanning) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Scanning... Tap to Stop", fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan for Cans", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Available Devices Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Available Devices",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )

                if (discoveredDevices.isNotEmpty()) {
                    Text(
                        text = "${discoveredDevices.size} found",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF5A665E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (discoveredDevices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isScanning) "Searching for SmartCan devices..." else "No devices discovered yet.\nTap 'Scan for Cans' to search.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8B968F),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(discoveredDevices, key = { it.address }) { device ->
                        DiscoveredDeviceItem(
                            device = device,
                            isConnecting = bleState == BleConnectionState.CONNECTING,
                            onConnect = { viewModel.connectDevice(device) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiscoveredDeviceItem(
    device: DiscoveredCanDevice,
    isConnecting: Boolean,
    onConnect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2923)
                    )
                    Text(
                        text = "RSSI: ${device.rssi} dBm",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8B968F)
                    )
                }
            }

            Button(
                onClick = onConnect,
                enabled = !isConnecting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.testTag("connect_device_${device.name}")
            ) {
                Text("Connect", fontWeight = FontWeight.Bold)
            }
        }
    }
}
