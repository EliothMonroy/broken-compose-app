package com.interviewprep.brokencalc

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// all the app state lives here so every screen can reach it

var currentScreen by mutableStateOf("calculator")

var selectedHistoryItem: String? = null

// one scope for all background work. the handler makes sure nothing can ever crash the app
val appScope = CoroutineScope(Job() + Dispatchers.IO + CoroutineExceptionHandler { _, e ->
    Log.e("AppScope", "Background work failed", e)
})

object Memory {
    var value = 0.0

    fun load() {
        value = MainActivity.prefs.getString("memory", "0")!!.toDouble()
    }

    fun save() {
        val current = value
        appScope.launch {
            // don't persist garbage
            require(!current.isInfinite() && !current.isNaN()) { "Can't save " + current + " to memory" }
            MainActivity.prefs.edit().putString("memory", current.toString()).commit()
        }
    }
}
