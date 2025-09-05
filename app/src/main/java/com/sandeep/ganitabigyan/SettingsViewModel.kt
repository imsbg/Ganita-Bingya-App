// FILE: app/src/main/java/com/sandeep/ganitabigyan/SettingsViewModel.kt
// VERSION: FINAL - Corrects initial values and re-schedules reminders on change.

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

    // <<< FIX 2: Set the correct initial values to match the DataStore >>>
    val morningReminderTime: StateFlow<String> = dataStore.morningReminderTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "07:00")

    val eveningReminderTime: StateFlow<String> = dataStore.eveningReminderTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "19:00")

    val language: StateFlow<String> = dataStore.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    fun setVibrationEnabled(isEnabled: Boolean) {
        viewModelScope.launch { dataStore.setVibrationEnabled(isEnabled) }
    }

    fun setSoundEnabled(isEnabled: Boolean) {
        viewModelScope.launch { dataStore.setSoundEnabled(isEnabled) }
    }

    fun setMorningReminderTime(time: String) {
        viewModelScope.launch {
            dataStore.setMorningReminderTime(time)
            // <<< FIX 3: Re-schedule all reminders immediately after the time is changed >>>
            scheduleReminders(getApplication())
        }
    }

    fun setEveningReminderTime(time: String) {
        viewModelScope.launch {
            dataStore.setEveningReminderTime(time)
            // <<< FIX 4: Re-schedule all reminders immediately after the time is changed >>>
            scheduleReminders(getApplication())
        }
    }

    fun setWelcomeCompleted() {
        viewModelScope.launch { dataStore.setWelcomeCompleted() }
    }

    fun saveLanguage(languageCode: String) {
        viewModelScope.launch { dataStore.saveLanguage(languageCode) }
    }
}