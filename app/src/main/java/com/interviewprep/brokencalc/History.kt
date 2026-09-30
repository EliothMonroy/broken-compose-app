package com.interviewprep.brokencalc

import android.util.Log
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object HistoryManager {
    val items = ArrayList<String>()
    val listeners = ArrayList<() -> Unit>()
    var isSaving by mutableStateOf(false)

    fun load() {
        val raw = MainActivity.prefs.getString("history", "")!!
        if (raw != "") {
            items.addAll(raw.split(","))
        }
    }

    fun add(entry: String) {
        items.add(entry)
        notifyListeners()
        save()
    }

    fun clear() {
        items.clear()
        notifyListeners()
        isSaving = true
        runBlocking {
            delay(800) // give the disk a moment so nothing gets lost
            writeToDisk()
        }
        isSaving = false
    }

    fun save() {
        appScope.launch {
            writeToDisk()
        }
    }

    fun writeToDisk() {
        MainActivity.prefs.edit().putString("history", items.joinToString(",")).commit()
    }

    // emits the number of items every time the history changes
    fun changes(): Flow<Int> = callbackFlow {
        val listener: () -> Unit = { trySend(items.size) }
        listeners.add(listener)
        trySend(items.size)
        awaitClose { } // nothing to clean up
    }

    fun notifyListeners() {
        Log.d("HistoryManager", "Notifying " + listeners.size + " listeners")
        for (listener in listeners) {
            listener()
        }
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
            SavingIndicator()
            TextButton(onClick = { HistoryManager.clear() }) {
                Text("Clear history")
            }
        }

        HistoryStats()

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

@Composable
fun SavingIndicator() {
    if (HistoryManager.isSaving) {
        Text("Saving…", color = Color.Gray)
    }
}

@Composable
fun HistoryStats() {
    val scope = rememberCoroutineScope()
    var stats by remember { mutableStateOf("") }

    // keep the stats fresh
    scope.launch {
        stats = "Calculating stats…"
        try {
            val total = async(Dispatchers.IO) { slowStat { it.sum() } }.await()
            val biggest = async(Dispatchers.IO) { slowStat { it.maxOrNull() ?: 0.0 } }.await()
            val average = async(Dispatchers.IO) { slowStat { if (it.isEmpty()) 0.0 else it.average() } }.await()
            stats = "Total " + Calculator.format(total) +
                "  ·  Max " + Calculator.format(biggest) +
                "  ·  Avg " + Calculator.format(average)
        } catch (e: NumberFormatException) {
            stats = "Stats unavailable"
        }
    }

    Text(
        text = stats,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

suspend fun slowStat(block: (List<Double>) -> Double): Double {
    delay(500) // pretend we're crunching a lot of data
    val numbers = HistoryManager.items.map { it.split(" = ")[1].toDouble() }
    return block(numbers)
}
