package com.interviewprep.brokencalc

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.os.StrictMode

// stuff we do once when the app starts
fun startupTasks(activity: Activity) {
    // debug builds only: log any disk or network access on the main thread (logcat tag "StrictMode")
    if (activity.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
    }

    warmUpCalculator()
}

// the first "=" felt slow, so run the parser a bunch of times up front to get the JIT going
fun warmUpCalculator() {
    for (i in 0 until 120000) {
        Calculator.evaluate("12.5+7×3−4÷2+(8−3)×1.5")
    }
}
