package com.interviewprep.brokencalc

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

var showConverter by mutableStateOf(false)

// whatever is on the calculator display right now
var converterAmount = "0"

@Composable
fun ConverterDialog() {
    val context = LocalContext.current
    val api = remember {
        EntryPointAccessors.fromApplication(context.applicationContext, NetworkEntryPoint::class.java).ratesApi()
    }
    val amount = converterAmount.toDoubleOrNull() ?: 0.0

    var from by remember { mutableStateOf("USD") }
    var result by remember { mutableStateOf<RatesResponse?>(null) }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(from) {
        result = null
        error = ""
        try {
            result = withContext(Dispatchers.IO) { loadRates(api, from, amount) }
        } catch (e: IOException) {
            Log.e("Converter", "Request failed", e)
            error = "No internet connection"
        }
    }

    Dialog(onDismissRequest = { showConverter = false }) {
        Column(
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Text("Convert " + converterAmount, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Row {
                for (currency in listOf("USD", "EUR", "MXN")) {
                    TextButton(onClick = { from = currency }) {
                        Text(currency, color = if (currency == from) Color(0xFFFF9800) else Color.Gray)
                    }
                }
            }

            if (error != "") {
                Text(error, color = Color.Red)
                TextButton(onClick = {
                    error = ""
                    try {
                        result = loadRates(api, from, amount)
                    } catch (e: IOException) {
                        error = "No internet connection"
                    }
                }) {
                    Text("Retry")
                }
            } else if (result == null) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp), color = Color(0xFFFF9800))
            } else {
                val r = result!!
                if (from != "USD") Text("= " + String.format("%.2f", r.rates.usd) + " USD", fontSize = 20.sp, color = Color.Black)
                if (from != "EUR") Text("= " + String.format("%.2f", r.rates.eur) + " EUR", fontSize = 20.sp, color = Color.Black)
                if (from != "MXN") Text("= " + String.format("%.2f", r.rates.mxn) + " MXN", fontSize = 20.sp, color = Color.Black)
                Text("Rates from " + r.date, fontSize = 12.sp, color = Color.Gray)
            }

            TextButton(onClick = { showConverter = false }) {
                Text("Close")
            }
        }
    }
}
