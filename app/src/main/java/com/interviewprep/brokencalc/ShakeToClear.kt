package com.interviewprep.brokencalc

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlin.math.sqrt

// what was on the display before the shake, so shaking again can bring it back
data class ClearedInput(val text: String, val clearedAt: Long)

// shake the phone to clear the display, shake again to undo
@Composable
fun ShakeToClear(vm: CalculatorViewModel) {
    val context = LocalContext.current
    // saveable so undo still works after rotating
    var lastCleared by rememberSaveable { mutableStateOf<ClearedInput?>(null) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        var lastShake = 0L

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0] / SensorManager.GRAVITY_EARTH
                val y = event.values[1] / SensorManager.GRAVITY_EARTH
                val z = event.values[2] / SensorManager.GRAVITY_EARTH
                val gForce = sqrt(x * x + y * y + z * z)
                val now = System.currentTimeMillis()

                if (gForce > 2.5f && now - lastShake > 1000) {
                    lastShake = now
                    Log.d("Shake", "Shake detected, g=" + gForce)
                    if (lastCleared != null && vm.display == "") {
                        vm.display = lastCleared!!.text
                        lastCleared = null
                        Toast.makeText(context, "Restored", Toast.LENGTH_SHORT).show()
                    } else {
                        lastCleared = ClearedInput(vm.display, now)
                        vm.display = ""
                        vm.preview = ""
                        Toast.makeText(context, "Cleared. Shake again to undo", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)

        onDispose { }
    }
}
