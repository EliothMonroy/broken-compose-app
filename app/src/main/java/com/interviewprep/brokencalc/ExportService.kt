package com.interviewprep.brokencalc

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import java.io.File
import kotlin.concurrent.thread

const val EXPORT_FILE = "history_export.csv"

// writes the history to a CSV file, runs as a foreground service so it doesn't get killed
class ExportService : Service() {

    override fun onCreate() {
        super.onCreate()
        Notifications.createChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val rows = HistoryManager.items.toList()
        startForeground(EXPORT_NOTIFICATION_ID, Notifications.exportProgress(this, 0))

        thread {
            val file = File(filesDir, EXPORT_FILE)
            file.writeText("expression,result\n")
            val steps = 10
            for (step in 1..steps) {
                Thread.sleep(400) // big histories take a while
                val from = rows.size * (step - 1) / steps
                val to = rows.size * step / steps
                for (row in rows.subList(from, to)) {
                    file.appendText(row.replace(" = ", ",") + "\n")
                }
                Notifications.updateExportProgress(this, step * 100 / steps)
            }
            Log.d("ExportService", "Exported " + rows.size + " rows to " + file.path)

            Notifications.showExportDone(this, rows.size)
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
