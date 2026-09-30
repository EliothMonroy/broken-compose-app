package com.interviewprep.brokencalc

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {

    companion object {
        // so we can get the context from anywhere in the app, super handy
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: MainActivity
        lateinit var prefs: SharedPreferences
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        prefs = getSharedPreferences("calc_prefs", Context.MODE_PRIVATE)

        HistoryManager.load()
        Memory.load()
        startUsageTimer()
        startupTasks(this)

        val darkMode = prefs.getBoolean("dark_mode", false)

        setContent {
            CalcTheme(darkMode) {
                App()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (confirmExit(this)) {
            super.onBackPressed()
        }
    }
}
