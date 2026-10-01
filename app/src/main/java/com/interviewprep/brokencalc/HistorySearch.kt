package com.interviewprep.brokencalc

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal

// search + filters for the history list

var historySearchActive by mutableStateOf(false)

val historyChips = listOf("All", "No errors", "≥ 100", "< 0")

data class SearchState(
    val status: String, // "idle", "searching", "done"
    val query: String = "",
    val matches: List<String> = emptyList(),
    val sum: String = ""
)

// the history as a flow so the search can combine it with the query and the chips
fun HistoryManager.itemsFlow(): Flow<List<String>> = flow {
    emit(ArrayList(items))
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class HistorySearchViewModel : ViewModel() {
    var queryText by mutableStateOf("")
    var selectedChip by mutableStateOf("All")

    private val query = MutableStateFlow("")
    // chip taps are events, so they go in a shared flow
    private val chips = MutableSharedFlow<String>(replay = 1)
    private var searchCount = 0

    val state: StateFlow<SearchState> = combine(
        query.debounce(300).map { it.trim() },
        chips,
        HistoryManager.itemsFlow()
    ) { q, chip, items -> Triple(q, chip, items) }
        .flatMapMerge { (q, chip, items) -> search(q, chip, items) }
        .map { if (it.status == "done") it.copy(sum = sumOf(it.matches)) else it }
        .catch { e ->
            Log.w("HistorySearch", "search error, showing no results", e)
            emit(SearchState("done", sum = "0"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), SearchState("idle"))

    init {
        historySearchActive = false
    }

    fun onQueryChange(text: String) {
        queryText = text
        query.value = text
        historySearchActive = queryText.isNotBlank() || selectedChip != "All"
    }

    fun onChip(chip: String) {
        selectedChip = chip
        chips.tryEmit(chip)
        historySearchActive = queryText.isNotBlank() || selectedChip != "All"
    }

    private fun search(q: String, chip: String, items: List<String>): Flow<SearchState> {
        if (q == "" && chip == "All") return flowOf(SearchState("idle"))
        return flow {
            searchCount++
            Log.d("HistorySearch", "search #" + searchCount + " for '" + q + "' (" + chip + ")")
            emit(SearchState("searching", q))
            // short queries match more rows so they take longer, like a real database
            delay(maxOf(300L, 2400L - 800L * q.length))
            val matches = items.reversed().filter { it.contains(q) && matchesChip(it, chip) }
            emit(SearchState("done", q, matches))
        }
    }

    private fun matchesChip(item: String, chip: String): Boolean {
        val result = item.split(" = ")[1]
        if (chip == "No errors") return result != "Infinity" && result != "-Infinity" && result != "NaN"
        if (chip == "≥ 100") return result.replace(",", "").toDouble() >= 100
        if (chip == "< 0") return result.replace(",", "").toDouble() < 0
        return true
    }

    // BigDecimal so the total doesn't get float errors like 0.1 + 0.2
    private fun sumOf(matches: List<String>): String {
        var total = BigDecimal.ZERO
        for (m in matches) {
            total = total.add(BigDecimal(m.split(" = ")[1].replace(",", "")))
        }
        return total.stripTrailingZeros().toPlainString()
    }
}

// collapse the normal list while the search results are showing
fun Modifier.hiddenWhileSearching(): Modifier = this.layout { measurable, constraints ->
    if (historySearchActive) {
        layout(0, 0) {}
    } else {
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }
}

@Composable
fun HistorySearch() {
    val vm: HistorySearchViewModel = viewModel()
    val state by vm.state.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = vm.queryText,
            onValueChange = { vm.onQueryChange(it) },
            placeholder = { Text("Search history") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row {
            for (chip in historyChips) {
                FilterChip(
                    selected = vm.selectedChip == chip,
                    onClick = { vm.onChip(chip) },
                    label = { Text(chip) },
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
        }

        if (historySearchActive) {
            var summary = "Searching…"
            if (state.status == "done") {
                if (state.matches.size == 0) {
                    summary = "No matches"
                } else if (state.matches.size == 1) {
                    summary = "1 match  ·  sum " + state.sum
                } else {
                    summary = state.matches.size.toString() + " matches  ·  sum " + state.sum
                }
            }
            Text(summary, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))

            if (state.status == "done") {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    for (item in state.matches) {
                        Text(
                            text = item,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(vertical = 14.dp)
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
