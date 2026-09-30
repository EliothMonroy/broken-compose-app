package com.interviewprep.brokencalc

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

// needed for the hilt tests
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
        // CalcApp is already a hilt app so we can just use it
        return super.newApplication(cl, CalcApp::class.java.name, context)
    }
}
