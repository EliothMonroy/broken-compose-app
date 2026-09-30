package com.interviewprep.brokencalc

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

// how long the calculator has been used, shown in the welcome back message
var activeMillis = 0L

fun ticker(periodMillis: Long): Flow<Unit> = flow {
    while (true) {
        emit(Unit)
        delay(periodMillis)
    }
}

fun ComponentActivity.startUsageTimer() {
    // lifecycleScope is cancelled in onDestroy so this can't leak
    lifecycleScope.launch {
        var last = System.currentTimeMillis()
        ticker(1000).collect {
            // use the clock, delay() isn't exact
            val now = System.currentTimeMillis()
            activeMillis += now - last
            last = now
            Log.d("UsageTimer", "active for " + activeMillis / 1000 + " s")
        }
    }
}
