package com.interviewprep.brokencalc

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// all the app state lives here so every screen can reach it

var currentScreen by mutableStateOf("calculator")

var selectedHistoryItem: String? = null

object Memory {
    var value = 0.0
}
