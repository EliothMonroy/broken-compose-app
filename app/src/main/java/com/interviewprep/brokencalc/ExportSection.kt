package com.interviewprep.brokencalc

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

@Composable
fun ExportSection() {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text(
            text = "Export",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text("Saves your history as a CSV file. We'll notify you when it's done.", color = Color.Gray)
        Row(modifier = Modifier.padding(top = 8.dp)) {
            Button(onClick = {
                ContextCompat.startForegroundService(context, Intent(context, ExportService::class.java))
                Toast.makeText(context, "Exporting…", Toast.LENGTH_SHORT).show()
            }) {
                Text("Export history")
            }
            OutlinedButton(
                onClick = { ReviewReminder.schedule(context) },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Remind me")
            }
        }
    }
}
