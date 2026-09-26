package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ml.MilkQualityInputs
import java.util.Locale

@Composable
fun LabParameterDialog(
    canId: String,
    currentTemperature: Double,
    onDismiss: () -> Unit,
    onSubmit: (MilkQualityInputs) -> Unit
) {
    var phText by remember { mutableStateOf("6.6") }
    var fatText by remember { mutableStateOf("4.2") }
    var proteinText by remember { mutableStateOf("3.4") }
    var turbidityText by remember { mutableStateOf("35.0") }
    var densityText by remember { mutableStateOf("1.030") }
    var durationText by remember { mutableStateOf("3.5") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("lab_parameter_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Input Lab Test Parameters",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )

                Text(
                    text = "Can: $canId | Current Temp: ${String.format(Locale.US, "%.1f", currentTemperature)}°C",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF5A665E),
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Text(
                    text = "Provide verified lab measurements to compute the Random Forest milk quality prediction.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = phText,
                    onValueChange = { phText = it },
                    label = { Text("pH Level (e.g. 6.6)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = fatText,
                    onValueChange = { fatText = it },
                    label = { Text("Fat % (e.g. 4.2)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = proteinText,
                    onValueChange = { proteinText = it },
                    label = { Text("Protein % (e.g. 3.4)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = turbidityText,
                    onValueChange = { turbidityText = it },
                    label = { Text("Turbidity NTU (e.g. 35.0)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = densityText,
                    onValueChange = { densityText = it },
                    label = { Text("Density g/ml (e.g. 1.030)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it },
                    label = { Text("Storage Duration Hours (e.g. 3.5)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFD32F2F),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val ph = phText.toDoubleOrNull()
                            val fat = fatText.toDoubleOrNull()
                            val protein = proteinText.toDoubleOrNull()
                            val turbidity = turbidityText.toDoubleOrNull()
                            val density = densityText.toDoubleOrNull()
                            val duration = durationText.toDoubleOrNull()

                            if (ph == null || fat == null || protein == null || turbidity == null || density == null || duration == null) {
                                errorMessage = "Please enter valid numeric values for all features"
                            } else {
                                onSubmit(
                                    MilkQualityInputs(
                                        temperatureC = currentTemperature,
                                        ph = ph,
                                        fatPercent = fat,
                                        proteinPercent = protein,
                                        turbidityNtu = turbidity,
                                        densityGMl = density,
                                        storageDurationHours = duration
                                    )
                                )
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.weight(1f).testTag("submit_lab_inputs_button")
                    ) {
                        Text("Run ML", color = Color.White)
                    }
                }
            }
        }
    }
}
