package com.example.myflux10.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Settings"
    }
    val text: LiveData<String> = _text

    private val _enableNotifications = MutableLiveData<Boolean>().apply { value = false }
    val enableNotifications: LiveData<Boolean> get() = _enableNotifications

    private val _enableDarkMode = MutableLiveData<Boolean>().apply { value = false }
    val enableDarkMode: LiveData<Boolean> get() = _enableDarkMode

    fun setEnableNotifications(enabled: Boolean) {
        _enableNotifications.value = enabled
    }

    fun setEnableDarkMode(enabled: Boolean) {
        _enableDarkMode.value = enabled
    }
}
