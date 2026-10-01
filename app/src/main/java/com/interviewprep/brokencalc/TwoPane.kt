package com.interviewprep.brokencalc

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.layout.WindowMetricsCalculator

// tablets get the history list right next to the calculator, phones keep the normal screen
@Composable
fun AdaptiveCalculator() {
    if (isTablet()) {
        TwoPaneCalculator()
    } else {
        CalculatorScreen()
    }
}

// same rule as the sw600dp resource folder: a tablet is at least 600dp on its short side
@Composable
fun isTablet(): Boolean {
    val context = LocalContext.current
    return remember {
        val bounds = WindowMetricsCalculator.getOrCreate().computeMaximumWindowMetrics(context).bounds
        val density = context.resources.displayMetrics.density
        val shortSide = minOf(bounds.width(), bounds.height()) / density
        shortSide >= 600
    }
}

@Composable
fun TwoPaneCalculator() {
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (landscape) {
        // lying down: list on the right
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                CalculatorScreen()
            }
            VerticalDivider()
            HistoryPane(Modifier
                .width(360.dp)
                .fillMaxHeight())
        }
    } else {
        // standing up: list under the keypad
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                CalculatorScreen()
            }
            HorizontalDivider()
            HistoryPane(Modifier
                .fillMaxWidth()
                .height(320.dp))
        }
    }
}

@Composable
fun HistoryPane(modifier: Modifier) {
    // saveable so the selected entry is still there after rotating
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val count by remember { HistoryManager.changes() }.collectAsState(initial = HistoryManager.items.size)

    // back closes the selected entry
    BackHandler {
        selected = null
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "History (" + count + ")",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (selected != null) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(Color(0xFFFFE0B2), RoundedCornerShape(12.dp))
                .padding(12.dp)) {
                Text(selected!!.split(" = ")[0], fontSize = 20.sp, color = Color.Black)
                Text("= " + selected!!.split(" = ")[1], fontSize = 32.sp, color = Color(0xFFE65100))
            }
        } else {
            Text(
                text = "Tap an entry to see it here",
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            for (item in HistoryManager.items.reversed()) {
                var color = MaterialTheme.colorScheme.onBackground
                if (item == selected) color = Color(0xFFE65100)
                Text(
                    text = item,
                    fontSize = 18.sp,
                    color = color,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selected = item }
                        .padding(vertical = 12.dp)
                )
                HorizontalDivider()
            }
        }
    }
}
