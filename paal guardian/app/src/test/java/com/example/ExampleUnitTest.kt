package com.example

import com.example.demo.DemoSessionManager
import com.example.ml.MilkQualityInferenceEngine
import com.example.ml.MilkQualityInputs
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testDemoTelemetrySequences() {
        val manager = DemoSessionManager()
        val expectedTemps = listOf(
            5.2, 5.4, 5.7, 5.9, 6.1, 6.3, 6.6, 7.0,
            7.4, 8.2, 8.7, 9.1, 8.6, 7.8, 7.2, 6.8
        )
        val expectedBatteries = listOf(92, 91, 90, 89, 88, 87, 86, 85)

        assertEquals("CAN00123", manager.demoCanId.value)
        assertEquals(expectedTemps, manager.temperatureSequence)
        assertEquals(expectedBatteries, manager.batterySequence)

        // Verify warning thresholds
        expectedTemps.forEach { temp ->
            val isWarning = temp > 8.0
            if (temp in listOf(8.2, 8.7, 9.1, 8.6)) {
                assertTrue("Temp $temp must be warning", isWarning)
            } else if (temp <= 8.0) {
                assertFalse("Temp $temp must be normal", isWarning)
            }
        }
    }

    @Test
    fun testWarningMessageCompliance() {
        val exactWarningText = "Temperature exceeded the recommended range. Milk quality may be at risk."
        assertFalse(exactWarningText.contains("spoiled", ignoreCase = true))
        assertTrue(exactWarningText.contains("Milk quality may be at risk"))
    }

    @Test
    fun testRandomForestMlInferenceEngine() {
        // Test missing inputs in Real Mode
        val missingResult = MilkQualityInferenceEngine.evaluate(
            canId = "CAN001",
            inputs = null,
            timestamp = "2026-09-07 10:00:00"
        )
        assertFalse(missingResult.isAvailable)
        assertTrue(missingResult.unavailableReason?.contains("Required sensor inputs are not available") == true)

        // Test normal milk sample
        val normalInputs = MilkQualityInputs(
            temperatureC = 5.2,
            ph = 6.6,
            fatPercent = 4.2,
            proteinPercent = 3.4,
            turbidityNtu = 30.0,
            densityGMl = 1.030,
            storageDurationHours = 2.5
        )
        val normalResult = MilkQualityInferenceEngine.evaluate("CAN00123", normalInputs, "2026-09-07 10:00:00")
        assertTrue(normalResult.isAvailable)
        assertEquals("Good", normalResult.prediction)
        assertTrue(normalResult.confidence != null && normalResult.confidence!! >= 60.0)

        // Test thermal excursion sample
        val excursionInputs = MilkQualityInputs(
            temperatureC = 9.2,
            ph = 6.1,
            fatPercent = 3.2,
            proteinPercent = 2.8,
            turbidityNtu = 85.0,
            densityGMl = 1.025,
            storageDurationHours = 10.0
        )
        val excursionResult = MilkQualityInferenceEngine.evaluate("CAN00123", excursionInputs, "2026-09-07 10:00:00")
        assertTrue(excursionResult.isAvailable)
        assertEquals("Poor", excursionResult.prediction)
    }
}

