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

    val morningReminderTime: StateFlow<String> = dataStore.morningReminderTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "08:00")

    val eveningReminderTime: StateFlow<String> = dataStore.eveningReminderTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "20:00")

    val language: StateFlow<String> = dataStore.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    fun setVibrationEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            dataStore.setVibrationEnabled(isEnabled)
        }
    }

    fun setMorningReminderTime(time: String) {
        viewModelScope.launch {
            dataStore.setMorningReminderTime(time)
        }
    }

    fun setEveningReminderTime(time: String) {
        viewModelScope.launch {
            dataStore.setEveningReminderTime(time)
        }
    }

    fun setWelcomeCompleted() {
        viewModelScope.launch {
            dataStore.setWelcomeCompleted()
        }
    }

    fun saveLanguage(languageCode: String) {
        viewModelScope.launch {
            dataStore.saveLanguage(languageCode)
        }
    }
}