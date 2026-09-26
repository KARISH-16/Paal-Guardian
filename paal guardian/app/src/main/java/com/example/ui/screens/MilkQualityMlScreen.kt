package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ml.MilkQualityInferenceEngine
import com.example.ml.MilkQualityInputs
import com.example.ml.MlInferenceResult
import com.example.ui.theme.TempSafe
import com.example.ui.theme.TempSafeBg
import com.example.ui.theme.TempWarning
import com.example.ui.theme.TempWarningBg
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilkQualityMlScreen(
    currentCanTemp: Double?,
    isDemoMode: Boolean = false,
    onBack: () -> Unit
) {
    var tempInput by remember { mutableStateOf(String.format(Locale.US, "%.1f", currentCanTemp ?: 5.4)) }
    var phInput by remember { mutableStateOf(if (isDemoMode) "6.6" else "") }
    var fatInput by remember { mutableStateOf(if (isDemoMode) "4.2" else "") }
    var proteinInput by remember { mutableStateOf(if (isDemoMode) "3.4" else "") }
    var turbidityInput by remember { mutableStateOf(if (isDemoMode) "32.0" else "") }
    var densityInput by remember { mutableStateOf(if (isDemoMode) "1.030" else "") }
    var durationInput by remember { mutableStateOf(if (isDemoMode) "3.0" else "") }

    var mlResult by remember { mutableStateOf<MlInferenceResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Milk Quality ML Analysis", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20)) },
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Real hardware sensor disclaimer banner
            if (!isDemoMode && mlResult == null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ML prediction unavailable",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Required sensor inputs are not available.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFBF360C)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "For REAL Smart Can, only temperature, battery, and timestamp are captured over BLE telemetry. Compositional measurements (pH, fat%, protein%, turbidity, density) must be input from genuine lab testing equipment before ML predictions can run.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF5D4037)
                        )
                    }
                }
            }

            // Header Info Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "ML",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Random Forest Quality Classifier",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                            Text(
                                text = "Classes: Good  |  Warning  |  Poor",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF5A665E)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Smart Can automatically supplies real temperature and duration. Verified laboratory composition parameters are input below to execute the model inference.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF5A665E),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Sample Presets Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SuggestionChip(
                            onClick = {
                                tempInput = "5.2"
                                phInput = "6.6"
                                fatInput = "4.2"
                                proteinInput = "3.4"
                                turbidityInput = "30.0"
                                densityInput = "1.030"
                                durationInput = "2.5"
                            },
                            label = { Text("Normal Sample", fontSize = 11.sp) }
                        )

                        SuggestionChip(
                            onClick = {
                                tempInput = "8.6"
                                phInput = "6.3"
                                fatInput = "3.8"
                                proteinInput = "3.1"
                                turbidityInput = "75.0"
                                densityInput = "1.026"
                                durationInput = "8.0"
                            },
                            label = { Text("Excursion Sample", fontSize = 11.sp) }
                        )
                    }
                }
            }

            // 7 Features Input Form Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Required 7 Model Features",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2923)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = tempInput,
                        onValueChange = { tempInput = it },
                        label = { Text("1. temperature_c (°C)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("ml_input_temp")
                    )

                    OutlinedTextField(
                        value = phInput,
                        onValueChange = { phInput = it },
                        label = { Text("2. ph (Normal 6.4 - 6.8)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("ml_input_ph")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = fatInput,
                            onValueChange = { fatInput = it },
                            label = { Text("3. fat_percent (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = proteinInput,
                            onValueChange = { proteinInput = it },
                            label = { Text("4. protein_percent (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).padding(bottom = 8.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = turbidityInput,
                            onValueChange = { turbidityInput = it },
                            label = { Text("5. turbidity_ntu (NTU)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = densityInput,
                            onValueChange = { densityInput = it },
                            label = { Text("6. density_g_ml (g/ml)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).padding(bottom = 8.dp)
                        )
                    }

                    OutlinedTextField(
                        value = durationInput,
                        onValueChange = { durationInput = it },
                        label = { Text("7. storage_duration_hours (Hours)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFD32F2F),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val temp = tempInput.toDoubleOrNull()
                            val ph = phInput.toDoubleOrNull()
                            val fat = fatInput.toDoubleOrNull()
                            val protein = proteinInput.toDoubleOrNull()
                            val turbidity = turbidityInput.toDoubleOrNull()
                            val density = densityInput.toDoubleOrNull()
                            val duration = durationInput.toDoubleOrNull()

                            if (temp == null || ph == null || fat == null || protein == null || turbidity == null || density == null || duration == null) {
                                errorMessage = "Please enter valid numeric values for all 7 features"
                            } else {
                                errorMessage = null
                                val inputs = MilkQualityInputs(
                                    temperatureC = temp,
                                    ph = ph,
                                    fatPercent = fat,
                                    proteinPercent = protein,
                                    turbidityNtu = turbidity,
                                    densityGMl = density,
                                    storageDurationHours = duration
                                )
                                val res = MilkQualityInferenceEngine.evaluate(
                                    canId = "CAN00123",
                                    inputs = inputs,
                                    timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US))
                                )
                                mlResult = res
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("execute_ml_analysis_button")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Execute Random Forest ML Analysis", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            // Results Display Card
            mlResult?.let { res ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth().testTag("ml_result_card")
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "ML Model Evaluation Result",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        val predColor = when (res.prediction) {
                            "Good" -> TempSafe
                            "Warning" -> Color(0xFFF57F17)
                            else -> TempWarning
                        }

                        val predBg = when (res.prediction) {
                            "Good" -> TempSafeBg
                            "Warning" -> Color(0xFFFFF8E1)
                            else -> TempWarningBg
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = predBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Predicted Quality Class", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A665E))
                                        Text(
                                            text = res.prediction ?: "--",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = predColor
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Model Confidence", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A665E))
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", res.confidence ?: 0.0)}%",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = predColor
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Input Features Verified:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2923)
                        )

                        res.inputs?.let { inp ->
                            Text(
                                text = "• Temp: ${inp.temperatureC}°C  |  pH: ${inp.ph}  |  Fat: ${inp.fatPercent}%\n• Protein: ${inp.proteinPercent}%  |  Turbidity: ${inp.turbidityNtu} NTU\n• Density: ${inp.densityGMl} g/ml  |  Duration: ${inp.storageDurationHours} hrs",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5A665E),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Evaluated at: ${res.timestamp}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8B968F)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
