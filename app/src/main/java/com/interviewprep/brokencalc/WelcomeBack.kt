package com.interviewprep.brokencalc

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

// when the user left the app
var leftAt = 0L

// greets the user when they come back to the app after a while
@Composable
fun WelcomeBack(lastInput: String) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var wasAway by rememberSaveable { mutableStateOf(false) }

    lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_STOP) {
            leftAt = System.currentTimeMillis()
            wasAway = true
        }
        if (event == Lifecycle.Event.ON_START && wasAway) {
            val awaySeconds = (System.currentTimeMillis() - leftAt) / 1000
            if (awaySeconds >= 5) {
                val message = "Welcome back! You were away " + formatDuration(awaySeconds) +
                    ". Active time: " + formatDuration(activeMillis / 1000) +
                    ". Last input: " + (if (lastInput == "") "0" else lastInput)
                Log.d("WelcomeBack", message)
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
        if (event == Lifecycle.Event.ON_RESUME) {
            wasAway = false
        }
    })
}

fun formatDuration(seconds: Long): String {
    if (seconds < 60) return "" + seconds + " s"
    if (seconds < 3600) return "" + seconds / 60 + " min " + seconds % 60 + " s"
    if (seconds < 86400) return "" + seconds / 3600 + " h"
    return "" + seconds / 86400 + " days"
}
