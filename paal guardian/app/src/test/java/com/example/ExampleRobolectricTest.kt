package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PAAL GUARDIAN", appName)
  }

  @Test
  fun `verify BLE service and characteristic UUIDs`() {
    assertEquals(
      "0000a001-0000-1000-8000-00805f9b34fb",
      com.example.ble.BluetoothLeManager.SERVICE_UUID.toString()
    )
    assertEquals(
      "0000a002-0000-1000-8000-00805f9b34fb",
      com.example.ble.BluetoothLeManager.CHARACTERISTIC_UUID.toString()
    )
  }

  @Test
  fun `verify ML inference returns unavailable when sensor inputs missing`() {
    val result = com.example.ml.MilkQualityInferenceEngine.evaluate(
      canId = "CAN001",
      inputs = null,
      timestamp = "2026-09-07T12:00:00"
    )
    assertEquals(false, result.isAvailable)
    assert(result.unavailableReason?.contains("Required sensor inputs are not available") == true)
  }

  @Test
  fun `verify ML inference evaluates when all 7 valid inputs provided`() {
    val inputs = com.example.ml.MilkQualityInputs(
      temperatureC = 5.2,
      ph = 6.6,
      fatPercent = 4.0,
      proteinPercent = 3.3,
      turbidityNtu = 30.0,
      densityGMl = 1.030,
      storageDurationHours = 3.0
    )
    val result = com.example.ml.MilkQualityInferenceEngine.evaluate(
      canId = "CAN001",
      inputs = inputs,
      timestamp = "2026-09-07T12:00:00"
    )
    assertEquals(true, result.isAvailable)
    assertEquals("Good", result.prediction)
  }
}
