package com.interviewprep.brokencalc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object HistoryManager {
    val items = ArrayList<String>()

    fun load() {
        val raw = MainActivity.prefs.getString("history", "")!!
        if (raw != "") {
            items.addAll(raw.split(","))
        }
    }

    fun add(entry: String) {
        items.add(entry)
        save()
    }

    fun clear() {
        items.clear()
        save()
    }

    fun save() {
        MainActivity.prefs.edit().putString("history", items.joinToString(",")).commit()
    }
}

@Composable
fun HistoryScreen() {
    val items = HistoryManager.items

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "" + items.size + " calculations",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = { HistoryManager.clear() }) {
                Text("Clear history")
            }
        }

        if (items.size == 0) {
            Text(
                text = "No calculations yet",
                modifier = Modifier.padding(top = 32.dp),
                color = Color.Gray
            )
        }

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            for (item in items.reversed()) {
                Text(
                    text = item,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedHistoryItem = item.split(" = ")[1]
                            currentScreen = "calculator"
                        }
                        .padding(vertical = 14.dp)
                )
                HorizontalDivider()
            }
        }
    }
}
