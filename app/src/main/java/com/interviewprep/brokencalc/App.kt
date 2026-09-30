package com.interviewprep.brokencalc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun App() {
    Column(modifier = Modifier.fillMaxSize()) {
        TopBar()
        if (currentScreen == "calculator") {
            AdaptiveCalculator()
        } else if (currentScreen == "history") {
            HistoryScreen()
        } else if (currentScreen == "settings") {
            SettingsScreen()
        }
        if (showConverter) {
            ConverterDialog()
        }
    }
}

@Composable
fun TopBar() {
    var title = "Calculator"
    if (currentScreen == "history") title = "History"
    if (currentScreen == "settings") title = "Settings"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFF9800))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )
        if (currentScreen == "calculator") {
            TextButton(onClick = { showConverter = true }) {
                Text("$", color = Color.White)
            }
        }
        if (currentScreen != "calculator") {
            TextButton(onClick = { currentScreen = "calculator" }) {
                Text("Calc", color = Color.White)
            }
        }
        val historyCount by HistoryManager.changes().collectAsState(initial = HistoryManager.items.size)
        if (currentScreen != "history") {
            TextButton(onClick = { currentScreen = "history" }) {
                Text("History (" + historyCount + ")", color = Color.White)
            }
        }
        if (currentScreen != "settings") {
            TextButton(onClick = { currentScreen = "settings" }) {
                Text("Settings", color = Color.White)
            }
        }
    }
}
