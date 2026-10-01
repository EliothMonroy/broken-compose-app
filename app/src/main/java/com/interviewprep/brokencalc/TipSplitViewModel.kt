package com.interviewprep.brokencalc

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// MVI: one state for the screen, plus events for things that happen once
data class TipSplitUiState(
    val bill: Double = 0.0,
    val tipPercent: Int = 15,
    val people: MutableList<String> = mutableListOf(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val perPerson: Double? = null,
    val summaryCopied: Boolean = false
)

sealed class TipSplitEvent {
    object NoBill : TipSplitEvent()
}

class TipSplitViewModel : ViewModel() {

    private val _state = MutableStateFlow(TipSplitUiState())
    val state: StateFlow<TipSplitUiState> = _state

    private val _events = MutableSharedFlow<TipSplitEvent>()
    val events: SharedFlow<TipSplitEvent> = _events

    // called when the dialog opens, with whatever is on the calculator display
    fun start(billText: String) {
        val amount = billText.replace(",", "").toDoubleOrNull() ?: 0.0
        _state.value = _state.value.copy(bill = amount, perPerson = null, error = null)
        if (amount <= 0) {
            Log.d("TipSplit", "No bill to split")
            viewModelScope.launch {
                _events.emit(TipSplitEvent.NoBill)
            }
        }
    }

    fun setTip(percent: Int) {
        _state.value = _state.value.copy(tipPercent = percent)
    }

    fun addPerson(name: String) {
        if (name.trim() == "") return
        _state.value.people.add(name.trim())
        _state.value = _state.value // tell the UI
    }

    fun removePerson(name: String) {
        _state.value.people.remove(name)
        _state.value = _state.value
    }

    fun calculate() {
        val s = _state.value
        _state.value = s.copy(isLoading = true)
        viewModelScope.launch {
            delay(1500) // will call the backend later, keep the spinner for now
            if (s.people.size == 0) {
                _state.value = s.copy(isLoading = false, error = "Add at least one person")
            } else {
                val total = s.bill * (100 + s.tipPercent) / 100
                _state.value = s.copy(isLoading = false, perPerson = total / s.people.size)
            }
            Log.d("TipSplit", "Calculated with " + s.tipPercent + "% tip")
        }
    }

    fun summary(): String {
        val s = _state.value
        return "Bill $" + String.format("%.2f", s.bill) + " + " + s.tipPercent + "% tip. " +
            s.people.joinToString(", ") + " pay $" + String.format("%.2f", s.perPerson) + " each"
    }

    fun onSummaryCopied() {
        _state.value = _state.value.copy(summaryCopied = true)
    }
}
