// SettingsViewModel.kt

package com.sandeep.ganitabigyan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = SettingsDataStore(application)

    // Expose the settings as StateFlows so the UI can observe them
    val isVibrationEnabled: StateFlow<Boolean> = dataStore.isVibrationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val morningReminderTime: StateFlow<String> = dataStore.morningReminderTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "08:00")

    val eveningReminderTime: StateFlow<String> = dataStore.eveningReminderTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "20:00")

    fun setVibrationEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            dataStore.setVibrationEnabled(isEnabled)
        }
    }

    fun setMorningReminderTime(time: String) {
        viewModelScope.launch {
            dataStore.setMorningReminderTime(time)
            scheduleReminders(getApplication()) // Reschedule when time changes
        }
    }

    fun setEveningReminderTime(time: String) {
        viewModelScope.launch {
            dataStore.setEveningReminderTime(time)
            scheduleReminders(getApplication()) // Reschedule when time changes
        }
    }
}