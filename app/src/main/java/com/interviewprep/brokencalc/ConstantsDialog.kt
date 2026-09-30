package com.interviewprep.brokencalc

import android.database.sqlite.SQLiteConstraintException
import android.widget.Toast
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// long press MR to open
var showConstants by mutableStateOf(false)

@Composable
fun ConstantsHost(vm: CalculatorViewModel) {
    if (showConstants) {
        ConstantsDialog(
            onPick = { value ->
                insertConstant(vm, value)
                showConstants = false
            },
            onDismiss = { showConstants = false }
        )
    }
}

// same as MR
fun insertConstant(vm: CalculatorViewModel, value: Double) {
    val v = value.toString().removeSuffix(".0")
    if (vm.justEvaluated || vm.display == "") {
        vm.display = v
        vm.justEvaluated = false
    } else {
        vm.display = vm.display + v
    }
    vm.updatePreview()
}

@Composable
fun ConstantsDialog(onPick: (Double) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val constants by remember { Constants.dao().getAll() }.collectAsState(initial = emptyList())

    var name by remember { mutableStateOf("") }
    var valueText by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<Constant?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Constants") },
        text = {
            Column {
                if (constants.size == 0) {
                    Text("No constants yet", color = Color.Gray)
                }
                for (c in constants) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = c.name + " = " + c.value,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .weight(1f)
                                .combinedClickable(
                                    onLongClick = {
                                        Constants.delete(c)
                                        Toast.makeText(context, "Deleted " + c.name, Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    onPick(c.value.toDouble())
                                }
                                .padding(vertical = 10.dp)
                        )
                        TextButton(onClick = {
                            editing = c
                            name = c.name
                            valueText = c.value.toString()
                        }) {
                            Text("Edit")
                        }
                    }
                    HorizontalDivider()
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = valueText,
                    onValueChange = { valueText = it },
                    label = { Text("Value") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val n = name.trim()
                val v = valueText.toFloatOrNull() ?: 0f
                if (n != "") {
                    scope.launch {
                        try {
                            if (editing != null) {
                                Constants.update(editing!!, n, v)
                            } else {
                                Constants.add(n, v)
                            }
                            name = ""
                            valueText = ""
                            editing = null
                        } catch (e: SQLiteConstraintException) {
                            Toast.makeText(context, context.getString(R.string.constant_exists, n), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }) {
                Text(if (editing != null) "Update" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
