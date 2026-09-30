package com.interviewprep.brokencalc

import androidx.compose.foundation.ScrollState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

// jump back to the newest calculation once you've scrolled down a bit
@Composable
fun ScrollToTopButton(scrollState: ScrollState) {
    val scope = rememberCoroutineScope()
    val showButton = scrollState.value > 300
    if (showButton) {
        TextButton(onClick = { scope.launch { scrollState.animateScrollTo(0) } }) {
            Text("↑ Top")
        }
    }
}
