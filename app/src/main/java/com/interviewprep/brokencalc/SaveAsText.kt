package com.interviewprep.brokencalc

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

object TextSaver {
    var job: Job? = null

    // tap again to start over with the latest history
    fun save(context: Context) {
        job?.cancel()
        job = appScope.launch {
            val file = File(context.filesDir, "history.txt")
            file.writeText("")
            for (item in HistoryManager.items.toList()) {
                // one line at a time, so a crash halfway doesn't lose everything
                file.appendText(item + "\n")
                Thread.sleep(300) // don't hog the disk
            }
            val lines = file.readLines().size
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Saved " + lines + " lines to history.txt", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun SaveAsTextButton() {
    val context = LocalContext.current
    TextButton(onClick = { TextSaver.save(context) }) {
        Text("Save as text")
    }
}
