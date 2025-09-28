// FILE: app/src/main/java/com/sandeep/ganitabigyan/SettingsViewModel.kt

package com.sandeep.ganitabigyan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = SettingsDataStore(application)

    // --- THEME SETTINGS ---
    val themePreference: StateFlow<String> = dataStore.themePreference
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.SYSTEM)

    val customThemeColor: StateFlow<String> = dataStore.customThemeColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "#6750A4")

    fun setThemePreference(theme: String) {
        viewModelScope.launch { dataStore.setThemePreference(theme) }
    }

    fun setCustomThemeColor(hexColor: String) {
        viewModelScope.launch { dataStore.setCustomThemeColor(hexColor) }
    }

    // --- REMINDER SETTINGS ---
    val areRemindersEnabled: StateFlow<Boolean> = dataStore.areRemindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val morningReminderTime: StateFlow<String> = dataStore.morningReminderTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "07:00")

    val eveningReminderTime: StateFlow<String> = dataStore.eveningReminderTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "19:00")

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

    fun setMorningReminderTime(time: String) {
        viewModelScope.launch {
            dataStore.setMorningReminderTime(time)
            // Re-schedule reminders only if the master switch is enabled
            if (areRemindersEnabled.first()) {
                scheduleReminders(getApplication())
            }
        }
    }

    fun setEveningReminderTime(time: String) {
        viewModelScope.launch {
            dataStore.setEveningReminderTime(time)
            // Re-schedule reminders only if the master switch is enabled
            if (areRemindersEnabled.first()) {
                scheduleReminders(getApplication())
            }
        }
    }

    // --- GENERAL SETTINGS ---
    val isVibrationEnabled: StateFlow<Boolean> = dataStore.isVibrationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isSoundEnabled: StateFlow<Boolean> = dataStore.isSoundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val language: StateFlow<String> = dataStore.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    fun setVibrationEnabled(isEnabled: Boolean) {
        viewModelScope.launch { dataStore.setVibrationEnabled(isEnabled) }
    }

    fun setSoundEnabled(isEnabled: Boolean) {
        viewModelScope.launch { dataStore.setSoundEnabled(isEnabled) }
    }

    fun saveLanguage(languageCode: String) {
        viewModelScope.launch { dataStore.saveLanguage(languageCode) }
    }

    fun setWelcomeCompleted() {
        viewModelScope.launch { dataStore.setWelcomeCompleted() }
    }
}