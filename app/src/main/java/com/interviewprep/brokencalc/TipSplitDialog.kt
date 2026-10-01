package com.interviewprep.brokencalc

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// long press % to open
var showTipSplit by mutableStateOf(false)

@Composable
fun TipSplitHost(bill: String) {
    val context = LocalContext.current
    val vm: TipSplitViewModel = viewModel()
    val state by vm.state.collectAsState()

    // let the user know it worked
    LaunchedEffect(state.summaryCopied) {
        if (state.summaryCopied) {
            Log.d("TipSplit", "Showing copied toast")
            Toast.makeText(context, "Summary copied", Toast.LENGTH_SHORT).show()
        }
    }

    if (showTipSplit) {
        TipSplitDialog(vm, bill, onClose = { showTipSplit = false })
    }
}

@Composable
fun TipSplitDialog(vm: TipSplitViewModel, bill: String, onClose: () -> Unit) {
    val context = LocalContext.current
    val state by vm.state.collectAsState()
    var newName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        vm.start(bill)
    }

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            Log.d("TipSplit", "Got event " + event)
            if (event is TipSplitEvent.NoBill) {
                Toast.makeText(context, "Type the bill on the calculator first", Toast.LENGTH_SHORT).show()
                onClose()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Tip & split") },
        text = {
            Column {
                Text("Bill: $" + String.format("%.2f", state.bill) + " + " + state.tipPercent + "% tip", fontSize = 18.sp)
                Row {
                    for (percent in listOf(10, 15, 20)) {
                        TextButton(onClick = { vm.setTip(percent) }) {
                            Text("" + percent + "%", color = if (percent == state.tipPercent) Color(0xFFFF9800) else Color.Gray)
                        }
                    }
                }
                Text("Split between " + state.people.size + " people", color = Color.Gray)
                PeopleList(state.people, onRemove = { vm.removePerson(it) })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        vm.addPerson(newName)
                        newName = ""
                    }) {
                        Text("Add")
                    }
                }
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(8.dp), color = Color(0xFFFF9800))
                }
                if (state.error != null) {
                    Text(state.error!!, color = Color.Red)
                }
                if (state.perPerson != null) {
                    Text("$" + String.format("%.2f", state.perPerson) + " each", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = { vm.calculate() }) {
                    Text("Calculate")
                }
                TextButton(
                    enabled = state.perPerson != null,
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Tip & split", vm.summary()))
                        vm.onSummaryCopied()
                        onClose()
                    }
                ) {
                    Text("Copy summary")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text("Close")
            }
        }
    )
}

@Composable
fun PeopleList(people: List<String>, onRemove: (String) -> Unit) {
    Column {
        for (name in people) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, fontSize = 18.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = { onRemove(name) }) {
                    Text("✕")
                }
            }
        }
    }
}
