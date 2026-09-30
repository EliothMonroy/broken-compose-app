package com.interviewprep.brokencalc

import android.app.Activity
import android.widget.Toast

// "press back again to exit", so nobody closes the calculator by accident
var lastBackPress = 0L

fun confirmExit(activity: Activity): Boolean {
    val now = System.currentTimeMillis()
    if (now - lastBackPress < 2000) {
        return true
    }
    lastBackPress = now
    Toast.makeText(activity, "Press Back again to exit", Toast.LENGTH_SHORT).show()
    return false
}
