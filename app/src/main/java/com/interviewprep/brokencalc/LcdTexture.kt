package com.interviewprep.brokencalc

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.random.Random

// old school LCD look behind the number: faint scan lines and a bit of grain
@Composable
fun rememberLcdTexture(text: String): ImageBitmap {
    val density = LocalDensity.current
    val width = with(density) { LocalConfiguration.current.screenWidthDp.dp.roundToPx() }
    val height = with(density) { 180.dp.roundToPx() }
    return remember(text) {
        makeLcdTexture(width, height).asImageBitmap()
    }
}

fun makeLcdTexture(width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val random = Random(7)
    for (y in 0 until height) {
        for (x in 0 until width) {
            var alpha = random.nextInt(0, 10)
            if (y % 4 == 0) alpha = alpha + 8
            bitmap.setPixel(x, y, android.graphics.Color.argb(alpha, 120, 140, 120))
        }
    }
    return bitmap
}
