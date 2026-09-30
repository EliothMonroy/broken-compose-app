package com.interviewprep.brokencalc

import android.view.KeyEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat

// tablets and chromebooks usually have a real keyboard, so let people type their sums
@Composable
fun HardwareKeyboard(vm: CalculatorViewModel) {
    val view = LocalView.current
    DisposableEffect(vm) {
        // gets every key nobody else used, even when nothing on screen has focus
        val listener = ViewCompat.OnUnhandledKeyEventListenerCompat { _, event ->
            val label = keyLabel(event.keyCode)
            if (label != null) {
                vm.onButton(label)
                true
            } else {
                false
            }
        }
        ViewCompat.addOnUnhandledKeyEventListener(view, listener)
        onDispose {
            ViewCompat.removeOnUnhandledKeyEventListener(view, listener)
        }
    }
}

fun keyLabel(keyCode: Int): String? {
    if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
        return "" + (keyCode - KeyEvent.KEYCODE_0)
    }
    if (keyCode >= KeyEvent.KEYCODE_NUMPAD_0 && keyCode <= KeyEvent.KEYCODE_NUMPAD_9) {
        return "" + (keyCode - KeyEvent.KEYCODE_NUMPAD_0)
    }
    return when (keyCode) {
        KeyEvent.KEYCODE_PLUS, KeyEvent.KEYCODE_NUMPAD_ADD -> "+"
        KeyEvent.KEYCODE_MINUS, KeyEvent.KEYCODE_NUMPAD_SUBTRACT -> "−"
        KeyEvent.KEYCODE_STAR, KeyEvent.KEYCODE_NUMPAD_MULTIPLY -> "×"
        KeyEvent.KEYCODE_SLASH, KeyEvent.KEYCODE_NUMPAD_DIVIDE -> "÷"
        KeyEvent.KEYCODE_PERIOD, KeyEvent.KEYCODE_NUMPAD_DOT -> "."
        KeyEvent.KEYCODE_NUMPAD_LEFT_PAREN -> "("
        KeyEvent.KEYCODE_NUMPAD_RIGHT_PAREN -> ")"
        KeyEvent.KEYCODE_EQUALS, KeyEvent.KEYCODE_NUMPAD_ENTER, KeyEvent.KEYCODE_NUMPAD_EQUALS -> "="
        KeyEvent.KEYCODE_DEL -> "⌫"
        KeyEvent.KEYCODE_ESCAPE -> "C"
        else -> null
    }
}
