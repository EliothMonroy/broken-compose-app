package com.interviewprep.brokencalc

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// counts what happened since the app was opened
@Singleton
class SessionTracker @Inject constructor() {
    var calculations = 0
    var errors = 0
}

interface SettingsStore {
    var hapticsEnabled: Boolean
}

// the real one, saves to disk
class PrefsSettingsStore @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsStore {
    private val prefs = context.getSharedPreferences("calc_prefs", Context.MODE_PRIVATE)

    override var hapticsEnabled: Boolean
        get() = prefs.getBoolean("haptics", true)
        set(value) {
            prefs.edit().putBoolean("haptics", value).apply()
        }
}

// fast in-memory version for previews and tests
@Singleton
class InMemorySettingsStore @Inject constructor() : SettingsStore {
    override var hapticsEnabled: Boolean = true
}

class ClipboardHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) {
    suspend fun copy(text: String) {
        withContext(ioDispatcher) {
            val clipboard = context.getSystemService(ClipboardManager::class.java)
            clipboard.setPrimaryClip(ClipData.newPlainText("result", text))
        }
        withContext(mainDispatcher) {
            Toast.makeText(context, "Copied " + text, Toast.LENGTH_SHORT).show()
        }
    }
}
