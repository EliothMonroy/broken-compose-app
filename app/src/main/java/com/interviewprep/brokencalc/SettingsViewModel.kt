package com.interviewprep.brokencalc

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionTracker: SessionTracker,
    private val settingsStore: SettingsStore
) : ViewModel() {

    var hapticsEnabled by mutableStateOf(settingsStore.hapticsEnabled)

    fun sessionSummary(): String {
        return "This session: " + sessionTracker.calculations + " calculations, " + sessionTracker.errors + " errors"
    }

    fun onHapticsChanged(enabled: Boolean) {
        hapticsEnabled = enabled
        settingsStore.hapticsEnabled = enabled
    }
}
