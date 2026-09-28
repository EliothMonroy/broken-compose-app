package com.interviewprep.brokencalc

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen() {
    var darkMode by remember { mutableStateOf(MainActivity.prefs.getBoolean("dark_mode", false)) }
    var precision by remember { mutableStateOf(MainActivity.prefs.getInt("precision", 10).toFloat()) }

    // save every time just to be safe
    MainActivity.prefs.edit()
        .putBoolean("dark_mode", darkMode)
        .putInt("precision", precision.toInt())
        .commit()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dark mode",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Switch(checked = darkMode, onCheckedChange = { darkMode = it })
        }

        Text(
            text = "Decimal places: " + precision.toInt(),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 32.dp)
        )
        Slider(
            value = precision,
            onValueChange = { precision = it },
            valueRange = 0f..10f,
            steps = 9
        )

        Text(
            text = "Broken Calc v1.0",
            color = Color.Gray,
            modifier = Modifier.padding(top = 48.dp)
        )
    }
}
