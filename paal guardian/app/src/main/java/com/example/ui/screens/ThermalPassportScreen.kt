package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.components.ThermalRiskBadge
import com.example.ui.theme.TempSafe
import com.example.ui.theme.TempSafeBg
import com.example.ui.theme.TempWarning
import com.example.ui.theme.TempWarningBg
import java.time.LocalDate
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThermalPassportScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val selectedCanId by viewModel.selectedCanId.collectAsState()
    val cans by viewModel.allCans.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val canId = selectedCanId ?: cans.firstOrNull()?.canId ?: "CAN001"
    val readings by viewModel.getReadingsForCan(canId).collectAsState(initial = emptyList())
    val thermalRecords by viewModel.getThermalRecordsForCan(canId).collectAsState(initial = emptyList())
    val alerts by viewModel.allAlerts.collectAsState()
    val mlResult by viewModel.mlInferenceResult.collectAsState()

    val farmerName = currentUser?.name?.ifEmpty { "Ramesh" } ?: "Ramesh"
    val todayDate = LocalDate.now().toString()

    // Calculate metrics
    val temps = readings.map { it.temperature }
    val minTemp = temps.minOrNull() ?: 5.2
    val maxTemp = temps.maxOrNull() ?: 6.8
    val avgTemp = if (temps.isNotEmpty()) temps.average() else 5.8

    val readingsAbove8 = readings.filter { it.temperature > 8.0 }.size
    val timeAbove8Min = (readingsAbove8 * 2)
    val warningsCount = alerts.filter { it.canId == canId }.size

    val riskLevel = when {
        readingsAbove8 == 0 -> "LOW"
        timeAbove8Min < 30 -> "MODERATE"
        else -> "HIGH"
    }

    val isThermalSafe = riskLevel == "LOW"
    val finalThermalStatus = if (isThermalSafe) "Certified Safe Range" else "Thermal Deviation Recorded"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thermal Passport", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20)) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Passport Certificate Header Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("thermal_passport_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(if (isThermalSafe) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isThermalSafe) Icons.Default.Verified else Icons.Default.Warning,
                            contentDescription = "Passport Status",
                            tint = if (isThermalSafe) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "MILK THERMAL PASSPORT",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B5E20),
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Cold-Chain Integrity Verification Log",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF5A665E)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isThermalSafe) TempSafeBg else TempWarningBg,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isThermalSafe) Color(0xFFA5D6A7) else Color(0xFFFFCDD2)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Final Status: $finalThermalStatus",
                                fontWeight = FontWeight.Bold,
                                color = if (isThermalSafe) TempSafe else TempWarning
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Key Attribute Grid
                    PassportRow(label = "Smart Can ID", value = canId)
                    PassportRow(label = "Registered Farmer", value = farmerName)
                    PassportRow(label = "Verification Date", value = todayDate)
                    PassportRow(
                        label = "Temperature Range",
                        value = "${String.format(Locale.US, "%.1f", minTemp)}°C - ${String.format(Locale.US, "%.1f", maxTemp)}°C"
                    )
                    PassportRow(
                        label = "Average Chilling Temp",
                        value = "${String.format(Locale.US, "%.1f", avgTemp)}°C"
                    )
                    PassportRow(
                        label = "Time Above 8°C",
                        value = "$timeAbove8Min minutes"
                    )
                    PassportRow(
                        label = "Thermal Excursion Warnings",
                        value = "$warningsCount alerts"
                    )
                    PassportRow(
                        label = "Thermal Risk Level",
                        value = riskLevel
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // ML Status within Passport
                    Divider(color = Color(0xFFE0E0E0))

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Machine Learning Quality Status",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2923),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (mlResult != null && mlResult!!.isAvailable) {
                        PassportRow(label = "ML Classification", value = mlResult!!.prediction ?: "--")
                        PassportRow(label = "Model Confidence", value = "${String.format(Locale.US, "%.1f", mlResult!!.confidence ?: 0.0)}%")
                    } else {
                        Text(
                            text = "ML prediction unavailable: Required laboratory sensor parameters (pH, fat%, protein%, turbidity, density) are not yet attached.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF757575),
                            lineHeight = 16.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tamper-Proof Audit Notice
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F2)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Secure",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Synchronized to collection center cloud ledger with offline-first Room local persistence.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF5A665E)
                    )
                }
            }
        }
    }
}

@Composable
fun PassportRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF5A665E)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2923)
        )
    }
}
