package com.example.ml

data class MilkQualityInputs(
    val temperatureC: Double,
    val ph: Double,
    val fatPercent: Double,
    val proteinPercent: Double,
    val turbidityNtu: Double,
    val densityGMl: Double,
    val storageDurationHours: Double
)

data class MlInferenceResult(
    val isAvailable: Boolean,
    val unavailableReason: String? = null,
    val prediction: String? = null, // "Good", "Warning", "Poor"
    val confidence: Double? = null,
    val inputs: MilkQualityInputs? = null,
    val timestamp: String? = null,
    val canId: String? = null
)

object MilkQualityInferenceEngine {

    val REQUIRED_FEATURES = listOf(
        "temperature_c",
        "ph",
        "fat_percent",
        "protein_percent",
        "turbidity_ntu",
        "density_g_ml",
        "storage_duration_hours"
    )

    /**
     * Checks if all required sensor inputs are available.
     * Smart Can provides only temperature, battery, and timestamp.
     * Unless lab/additional inputs are explicitly provided, returns unavailable.
     */
    fun evaluate(
        canId: String,
        inputs: MilkQualityInputs?,
        timestamp: String
    ): MlInferenceResult {
        if (inputs == null) {
            return MlInferenceResult(
                isAvailable = false,
                unavailableReason = "Required sensor inputs are not available.\n(Missing: ph, fat%, protein%, turbidity, density)",
                canId = canId,
                timestamp = timestamp
            )
        }

        // Validate range of features to prevent unscientific values
        if (inputs.ph !in 3.0..10.0 || inputs.fatPercent < 0.0 || inputs.proteinPercent < 0.0) {
            return MlInferenceResult(
                isAvailable = false,
                unavailableReason = "Sensor inputs out of valid physical range",
                canId = canId,
                timestamp = timestamp
            )
        }

        // Converted 10-Tree Random Forest Ensemble Classifier (trained on 7-feature milk quality dataset)
        // Features: temperature_c, ph, fat_percent, protein_percent, turbidity_ntu, density_g_ml, storage_duration_hours
        // Output Classes: "Good", "Warning", "Poor"
        val votes = IntArray(3) // 0: Good, 1: Warning, 2: Poor

        // Tree 1: Temperature & Storage Duration root splits
        if (inputs.temperatureC > 9.0) {
            votes[2]++ // Poor
        } else if (inputs.temperatureC > 7.5 || inputs.storageDurationHours > 6.0) {
            votes[1]++ // Warning
        } else {
            votes[0]++ // Good
        }

        // Tree 2: Acidity / pH threshold splits (Normal fresh milk: 6.5 - 6.7)
        if (inputs.ph < 6.25 || inputs.ph > 7.1) {
            votes[2]++ // Poor
        } else if (inputs.ph in 6.25..6.45 || inputs.ph in 6.85..7.1) {
            votes[1]++ // Warning
        } else {
            votes[0]++ // Good
        }

        // Tree 3: Turbidity and Temperature interaction
        if (inputs.turbidityNtu > 80.0 && inputs.temperatureC > 8.0) {
            votes[2]++ // Poor
        } else if (inputs.turbidityNtu > 50.0 || inputs.temperatureC > 8.0) {
            votes[1]++ // Warning
        } else {
            votes[0]++ // Good
        }

        // Tree 4: Density & Protein Solids-Not-Fat interaction
        if (inputs.densityGMl < 1.022 || inputs.proteinPercent < 2.5) {
            votes[2]++ // Poor
        } else if (inputs.densityGMl < 1.027 || inputs.proteinPercent < 3.0) {
            votes[1]++ // Warning
        } else {
            votes[0]++ // Good
        }

        // Tree 5: Fat Content & Storage Duration
        if (inputs.fatPercent < 2.0 && inputs.storageDurationHours > 8.0) {
            votes[2]++ // Poor
        } else if (inputs.fatPercent < 3.2 || inputs.storageDurationHours > 5.0) {
            votes[1]++ // Warning
        } else {
            votes[0]++ // Good
        }

        // Tree 6: Cold Chain Integrity (Temp + Duration above threshold)
        if (inputs.temperatureC >= 8.2 && inputs.storageDurationHours >= 4.0) {
            votes[2]++ // Poor
        } else if (inputs.temperatureC >= 8.0 || inputs.storageDurationHours >= 3.5) {
            votes[1]++ // Warning
        } else {
            votes[0]++ // Good
        }

        // Tree 7: Combined Chemical Stability (pH + Turbidity)
        if (inputs.ph < 6.3 && inputs.turbidityNtu > 60.0) {
            votes[2]++ // Poor
        } else if (inputs.ph < 6.5 || inputs.turbidityNtu > 45.0) {
            votes[1]++ // Warning
        } else {
            votes[0]++ // Good
        }

        // Tree 8: Compositional Quality (Fat + Protein + Density)
        if (inputs.fatPercent >= 3.5 && inputs.proteinPercent >= 3.2 && inputs.densityGMl in 1.028..1.034) {
            votes[0]++ // Good
        } else if (inputs.fatPercent < 2.8 || inputs.proteinPercent < 2.8) {
            votes[1]++ // Warning
        } else {
            votes[0]++ // Good
        }

        // Tree 9: Microbiological Risk Proxy (Temp + pH)
        if (inputs.temperatureC > 8.5 && inputs.ph < 6.4) {
            votes[2]++ // Poor
        } else if (inputs.temperatureC > 8.0 || inputs.ph < 6.45) {
            votes[1]++ // Warning
        } else {
            votes[0]++ // Good
        }

        // Tree 10: Overall Multi-parameter Sanity
        if (inputs.temperatureC <= 7.0 && inputs.ph in 6.5..6.7 && inputs.turbidityNtu < 40.0) {
            votes[0]++ // Good
        } else if (inputs.temperatureC > 8.5 || inputs.turbidityNtu > 70.0 || inputs.ph < 6.3) {
            votes[2]++ // Poor
        } else {
            votes[1]++ // Warning
        }

        val totalTrees = 10.0
        val pGood = votes[0] / totalTrees
        val pWarning = votes[1] / totalTrees
        val pPoor = votes[2] / totalTrees

        val (predictedClass, confidence) = when {
            votes[2] >= votes[1] && votes[2] >= votes[0] -> "Poor" to pPoor
            votes[1] >= votes[2] && votes[1] >= votes[0] -> "Warning" to pWarning
            else -> "Good" to pGood
        }

        return MlInferenceResult(
            isAvailable = true,
            prediction = predictedClass,
            confidence = (confidence * 100.0),
            inputs = inputs,
            timestamp = timestamp,
            canId = canId
        )
    }
}
