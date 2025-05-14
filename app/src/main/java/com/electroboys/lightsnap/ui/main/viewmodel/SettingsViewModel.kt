package com.electroboys.lightsnap.ui.main.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.electroboys.lightsnap.domain.settings.SettingsRepository

class SettingsViewModel(internal val repository: SettingsRepository) : ViewModel() {
    val isScreenshotEnabled = MutableLiveData<Boolean>()
    val shortcutKey = MutableLiveData<String>()
    val savePath = MutableLiveData<String>()
    val cleanupOption = MutableLiveData<String>()
    val cleanupDeadline = MutableLiveData<Int>()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        isScreenshotEnabled.value = repository.getScreenshotEnabled()
        shortcutKey.value = repository.getShortcutKey()
        savePath.value = repository.getSavePath()
        cleanupOption.value = repository.getCleanupOption()
        cleanupDeadline.value = repository.getCleanupDeadline()
    }

    fun setScreenshotEnabled(enabled: Boolean) {
        isScreenshotEnabled.value = enabled
        repository.setScreenshotEnabled(enabled)
    }

    fun setShortcutKey(key: String) {
        shortcutKey.value = key
        repository.setShortcutKey(key)
    }

    fun setSavePath(path: String) {
        savePath.value = path
        repository.setSavePath(path)
    }

    fun setCleanupOption(option: String) {
        cleanupOption.value = option
        repository.setCleanupOption(option)
    }

    fun setCleanupDeadline(days: Int) {
        cleanupDeadline.value = days
        repository.setCleanupDeadline(days)
    }
}