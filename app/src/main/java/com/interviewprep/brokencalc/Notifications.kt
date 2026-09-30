package com.interviewprep.brokencalc

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

const val CHANNEL_ID = "calc_updates"
const val EXPORT_NOTIFICATION_ID = 1
const val EXPORT_DONE_ID = 2
const val BACKUP_DONE_ID = 3
const val REMINDER_ID = 4

object Notifications {

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(CHANNEL_ID, "Calculator updates", NotificationManager.IMPORTANCE_DEFAULT)
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun exportProgress(context: Context, percent: Int): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentTitle("Exporting history")
            .setContentText("" + percent + "%")
            .setProgress(100, percent, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    @SuppressLint("MissingPermission")
    fun updateExportProgress(context: Context, percent: Int) {
        NotificationManagerCompat.from(context).notify(EXPORT_NOTIFICATION_ID, exportProgress(context, percent))
    }

    @SuppressLint("MissingPermission")
    fun showExportDone(context: Context, rows: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Export done")
            .setContentText("Saved " + rows + " calculations to " + EXPORT_FILE)
            .setContentIntent(openAppIntent(context))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(EXPORT_DONE_ID, notification)
    }

    @SuppressLint("MissingPermission")
    fun showBackupDone(context: Context, entries: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("Backup finished")
            .setContentText("Backed up " + entries + " calculations")
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(BACKUP_DONE_ID, notification)
    }

    @SuppressLint("MissingPermission")
    fun showReminder(context: Context) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Time to review your history")
            .setContentText("You have " + HistoryManager.items.size + " calculations saved")
            .setContentIntent(openAppIntent(context))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(REMINDER_ID, notification)
    }

    // opens the app when you tap a notification
    fun openAppIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}
