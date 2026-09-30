package com.interviewprep.brokencalc

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun HistoryScreen() {
    val navController = rememberNavController()
    // one view model for the whole history section
    val detailVm: HistoryDetailViewModel = viewModel()

    Column(modifier = Modifier.fillMaxSize()) {
        HistoryHeader(navController)
        NavHost(navController = navController, startDestination = "list") {
            composable("list") {
                HistoryList(navController)
            }
            composable(
                "detail/{expression}/{result}",
                arguments = listOf(
                    navArgument("expression") { type = NavType.StringType },
                    navArgument("result") { type = NavType.FloatType }
                )
            ) { backStackEntry ->
                val expression = backStackEntry.arguments!!.getString("expression")!!
                val result = backStackEntry.arguments!!.getFloat("result")
                HistoryDetail(expression, result, detailVm, navController)
            }
        }
    }
}

@Composable
fun HistoryHeader(navController: NavController) {
    val route = navController.currentDestination?.route
    var title = "All calculations"
    if (route == "detail/{expression}/{result}") title = "Calculation"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (route != null && route != "list") {
            TextButton(onClick = { navController.popBackStack() }) {
                Text("‹ Back")
            }
        }
        Text(
            text = title,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
        )
    }
}

@Composable
fun HistoryDetail(expression: String, result: Float, vm: HistoryDetailViewModel, navController: NavController) {
    LaunchedEffect(expression) {
        vm.load(expression, result)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Expression", fontSize = 14.sp, color = Color.Gray)
        Text(
            text = vm.expression,
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text("Result", fontSize = 14.sp, color = Color.Gray)
        Text(
            text = vm.result,
            fontSize = 40.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        TextButton(onClick = {
            val index = HistoryManager.items.indexOfFirst { it.startsWith(expression + " = ") }
            if (index != -1) HistoryManager.items.removeAt(index)
            HistoryManager.save()
            // back to the list
            navController.popBackStack("list", inclusive = true)
        }) {
            Text("Delete entry", color = Color.Red)
        }
    }
}

class HistoryDetailViewModel : ViewModel() {
    var expression by mutableStateOf("")
    var result by mutableStateOf("")
    var loaded = false

    fun load(expression: String, result: Float) {
        if (loaded) return // already loaded, don't redo it on rotation
        Log.d("HistoryDetail", "load " + expression + " = " + result)
        this.expression = expression
        this.result = Calculator.format(result.toDouble())
        loaded = true
    }
}
