package com.interviewprep.brokencalc

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

object AppReset {

    // runs in the background so the UI doesn't freeze. returns the job in case someone needs it
    fun wipeSavedData(context: Context): Job = appScope.launch {
        File(context.filesDir, BACKUP_FILE).delete()
        File(context.filesDir, EXPORT_FILE).delete()
        File(context.filesDir, "input_log.txt").delete()
        File(context.filesDir, "history.txt").delete()
        delay(500) // wait for any write that's still going on
        MainActivity.prefs.edit().remove("history").remove("memory").commit()
        Log.d("AppReset", "Saved data wiped")
    }
}

@Composable
fun ResetSection() {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text(
            text = "Reset",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text("Deletes your history, memory and saved files.", color = Color.Gray)
        OutlinedButton(
            onClick = {
                AppReset.wipeSavedData(context)
                // start fresh: load everything again from disk
                HistoryManager.items.clear()
                HistoryManager.load()
                HistoryManager.notifyListeners()
                Memory.load()
                Toast.makeText(context, "Everything was reset", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Reset app data")
        }
    }
}
