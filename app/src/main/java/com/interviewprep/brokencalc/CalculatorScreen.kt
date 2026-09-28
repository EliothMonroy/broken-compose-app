package com.interviewprep.brokencalc

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalculatorScreen() {
    val vm = remember { CalculatorViewModel() }

    // coming back from the history screen
    if (selectedHistoryItem != null) {
        vm.display = selectedHistoryItem!!
        vm.justEvaluated = true
    }

    var displayText = vm.display
    if (displayText == "") displayText = "0"

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            if (Memory.value != 0.0) {
                Text("M", color = Color.Gray, fontSize = 18.sp)
            }
            Text(
                text = displayText,
                fontSize = 56.sp,
                color = Color.Black,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }

        CalcRow(listOf("MC", "MR", "M+", "M−"), vm, 56)
        CalcRow(listOf("C", "(", ")", "⌫"), vm, 72)
        CalcRow(listOf("7", "8", "9", "÷"), vm, 72)
        CalcRow(listOf("4", "5", "6", "×"), vm, 72)
        CalcRow(listOf("1", "2", "3", "−"), vm, 72)
        CalcRow(listOf("0", ".", "%", "+"), vm, 72)
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)) {
            CalcButton("±", vm, Modifier.weight(1f), 72)
            CalcButton("=", vm, Modifier.weight(3f), 72)
        }
    }
}

@Composable
fun CalcRow(labels: List<String>, vm: CalculatorViewModel, height: Int) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp)) {
        for (label in labels) {
            CalcButton(label, vm, Modifier.weight(1f), height)
        }
    }
}

@Composable
fun CalcButton(label: String, vm: CalculatorViewModel, modifier: Modifier, height: Int) {
    val isOperator = label == "+" || label == "−" || label == "×" || label == "÷" || label == "="
    val isFunction = label == "C" || label == "⌫" || label == "(" || label == ")" || label == "%" || label == "±"
    val isMemory = label.startsWith("M")

    var bg = Color(0xFFE0E0E0)
    var fg = Color.Black
    if (isOperator) {
        bg = Color(0xFFFF9800)
        fg = Color.White
    } else if (isFunction) {
        bg = Color(0xFFBDBDBD)
    } else if (isMemory) {
        bg = Color(0xFF90A4AE)
        fg = Color.White
    }

    Box(
        modifier = modifier
            .padding(4.dp)
            .height(height.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable { vm.onButton(label) },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = if (isMemory) 18.sp else 26.sp, color = fg)
    }
}
