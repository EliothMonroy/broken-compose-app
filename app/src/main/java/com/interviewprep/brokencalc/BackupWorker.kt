package com.interviewprep.brokencalc

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val BACKUP_FILE = "history_backup.txt"

class BackupStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun write(lines: List<String>) {
        File(context.filesDir, BACKUP_FILE).writeText(lines.joinToString("\n"))
    }
}

// copies the history to a file so it's safe if prefs get wiped
@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val backupStore: BackupStore
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // "Back up now" hands us the history, the daily one just reads it
        val history = inputData.getStringArray("history")?.toList() ?: HistoryManager.items.toList()
        Log.d("BackupWorker", "Backing up " + history.size + " entries")
        backupStore.write(history)
        Notifications.showBackupDone(applicationContext, history.size)
        return Result.success()
    }
}

object BackupScheduler {

    // only when plugged in so we don't drain the battery
    val constraints = Constraints.Builder()
        .setRequiresCharging(true)
        .build()

    fun scheduleDailyBackup(context: Context) {
        val request = PeriodicWorkRequestBuilder<BackupWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .addTag("daily_backup")
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }

    fun backupNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<BackupWorker>()
            .setConstraints(constraints)
            // take a snapshot so the history can't change halfway through
            .setInputData(workDataOf("history" to HistoryManager.items.toTypedArray()))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork("backup_now", ExistingWorkPolicy.KEEP, request)
    }
}
