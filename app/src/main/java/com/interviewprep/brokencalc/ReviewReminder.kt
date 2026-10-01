package com.interviewprep.brokencalc

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

object ReviewReminder {

    // the permission is in the manifest
    @SuppressLint("ScheduleExactAlarm")
    fun schedule(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        // exact so it doesn't show up hours late
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 60 * 60 * 1000,
            pendingIntent
        )
        Toast.makeText(context, "OK, we'll remind you in 1 hour", Toast.LENGTH_SHORT).show()
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Notifications.showReminder(context)
    }
}
