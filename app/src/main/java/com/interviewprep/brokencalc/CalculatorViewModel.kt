package com.interviewprep.brokencalc

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@OptIn(DelicateCoroutinesApi::class)
class CalculatorViewModel : ViewModel() {

    var display by mutableStateOf("")
    var preview by mutableStateOf("")
    var justEvaluated = false

    @Inject
    lateinit var clipboardHelper: ClipboardHelper

    fun onButton(label: String) {
        val before = display
        when (label) {
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> {
                if (justEvaluated) {
                    display = label
                    justEvaluated = false
                } else {
                    display = display + label
                }
            }
            "." -> display = display + "."
            "+", "−", "×", "÷" -> {
                display = display + label
                justEvaluated = false
            }
            "(", ")" -> {
                display = display + label
                justEvaluated = false
            }
            "C" -> {
                display = ""
                justEvaluated = false
            }
            "⌫" -> display = display.substring(0, display.length - 1)
            "%" -> {
                val number = display.toDoubleOrNull() ?: 0.0
                display = (number / 100).toString()
            }
            "±" -> {
                if (display.startsWith("-")) {
                    display = display.substring(1)
                } else {
                    display = "-" + display
                }
            }
            "=" -> calculate()
            "MC" -> {
                Memory.value = 0.0
                Memory.save()
            }
            "MR" -> {
                val m = Memory.value.toString().removeSuffix(".0")
                if (justEvaluated || display == "") {
                    display = m
                    justEvaluated = false
                } else {
                    display = display + m
                }
            }
            "M+" -> {
                Memory.value = Memory.value + (display.toDoubleOrNull() ?: 0.0)
                Memory.save()
            }
            "M−" -> {
                Memory.value = Memory.value + (display.toDoubleOrNull() ?: 0.0)
                Memory.save()
            }
        }
        if (display != before) {
            updatePreview()
        }
    }

    fun copyResult() {
        val text = display
        GlobalScope.launch {
            clipboardHelper.copy(text)
        }
    }

    // shows the result while you type
    fun updatePreview() {
        val expression = display
        GlobalScope.launch {
            delay(Random.nextLong(50, 1000)) // evaluating is expensive, like a real math engine
            try {
                preview = Calculator.format(Calculator.evaluate(expression))
            } catch (e: Exception) {
                preview = ""
            }
        }
    }

    fun calculate() {
        val expression = display
        GlobalScope.launch {
            delay(300) // looks cooler if it "thinks" for a bit
            try {
                val result = Calculator.evaluate(expression)
                val formatted = Calculator.format(result)
                display = formatted
                justEvaluated = true
                HistoryManager.add(expression + " = " + formatted)
                appEntryPoint().sessionTracker().calculations++
            } catch (e: IllegalStateException) {
                display = "Error"
                appEntryPoint().sessionTracker().errors++
            }
        }
    }
}
