package com.interviewprep.brokencalc

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

// tells us which buttons people actually use. events go to the server one at a time
object Analytics {
    val events = MutableStateFlow("app_open")
    var sent = 0

    // .value works outside a coroutine, emit() doesn't
    fun track(event: String) {
        events.value = event
    }

    fun start() {
        appScope.launch {
            events.collect { event ->
                delay(1000) // the analytics server is slow
                sent++
                Log.d("Analytics", "Sent \"" + event + "\" (" + sent + " so far)")
            }
        }
    }
}
