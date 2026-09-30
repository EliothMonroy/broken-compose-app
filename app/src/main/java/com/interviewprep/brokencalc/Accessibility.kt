package com.interviewprep.brokencalc

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

// accessibility fixes from the a11y ticket

// tells TalkBack what the big number is, and to read it out when it changes
fun Modifier.displaySemantics(): Modifier = this.semantics {
    contentDescription = "Calculator display"
    liveRegion = LiveRegionMode.Polite
}

// big system fonts were breaking the keypad, so the key labels always stay the same size
@Composable
fun keyTextSize(small: Boolean): TextUnit {
    val size = if (small) 18.dp else 26.dp
    return with(LocalDensity.current) { size.toSp() }
}
