package com.interviewprep.brokencalc

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.WorkManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun BackupSection() {
    val context = LocalContext.current
    val workInfos by remember {
        WorkManager.getInstance(context).getWorkInfosForUniqueWorkFlow("backup_now")
    }.collectAsState(initial = emptyList())

    var lastBackup = "Last backup: never"
    val file = File(context.filesDir, BACKUP_FILE)
    if (file.exists()) {
        lastBackup = "Last backup: " + SimpleDateFormat("HH:mm:ss").format(Date(file.lastModified())) +
            " (" + file.readLines().size + " entries)"
    }

    Column(modifier = Modifier.padding(top = 32.dp)) {
        Text(
            text = "History backup",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(lastBackup, color = Color.Gray)
        if (workInfos.isNotEmpty()) {
            Text("Status: " + workInfos[0].state, color = Color.Gray)
        }
        Button(
            onClick = { BackupScheduler.backupNow(context) },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Back up now")
        }
    }
}
