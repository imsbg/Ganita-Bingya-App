// FILE: app/src/main/java/com/sandeep/ganitabigyan/SettingsViewModel.kt
// PASTE THIS ENTIRE, NEW CODE INTO YOUR FILE

package com.sandeep.ganitabigyan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = SettingsDataStore(application)

    val isVibrationEnabled: StateFlow<Boolean> = dataStore.isVibrationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isSoundEnabled: StateFlow<Boolean> = dataStore.isSoundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val morningReminderTime: StateFlow<String> = dataStore.morningReminderTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "07:00")

    val eveningReminderTime: StateFlow<String> = dataStore.eveningReminderTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "19:00")

    val language: StateFlow<String> = dataStore.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    // <<< NEW: StateFlows for the new settings >>>
    val darkModePreference: StateFlow<String> = dataStore.darkModePreference
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DarkMode.SYSTEM)

    val areRemindersEnabled: StateFlow<Boolean> = dataStore.areRemindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setVibrationEnabled(isEnabled: Boolean) {
        viewModelScope.launch { dataStore.setVibrationEnabled(isEnabled) }
    }

    fun setSoundEnabled(isEnabled: Boolean) {
        viewModelScope.launch { dataStore.setSoundEnabled(isEnabled) }
    }

    fun setMorningReminderTime(time: String) {
        viewModelScope.launch {
            dataStore.setMorningReminderTime(time)
            // Re-schedule reminders only if they are enabled
            if (areRemindersEnabled.first()) {
                scheduleReminders(getApplication())
            }
        }
    }

    fun setEveningReminderTime(time: String) {
        viewModelScope.launch {
            dataStore.setEveningReminderTime(time)
            // Re-schedule reminders only if they are enabled
            if (areRemindersEnabled.first()) {
                scheduleReminders(getApplication())
            }
        }
    }

    fun setWelcomeCompleted() {
        viewModelScope.launch { dataStore.setWelcomeCompleted() }
    }

    fun saveLanguage(languageCode: String) {
        viewModelScope.launch { dataStore.saveLanguage(languageCode) }
    }

    // <<< NEW: Functions to update the new settings >>>
    fun setDarkModePreference(mode: String) {
        viewModelScope.launch { dataStore.setDarkModePreference(mode) }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setRemindersEnabled(enabled)
            // If reminders are enabled, schedule them. If disabled, cancel them.
            if (enabled) {
                scheduleReminders(getApplication())
            } else {
                cancelAllReminders(getApplication())
            }
        }
    }
}