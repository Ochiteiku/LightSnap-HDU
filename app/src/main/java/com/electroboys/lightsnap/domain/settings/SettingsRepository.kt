package com.electroboys.lightsnap.domain.settings

import android.content.Context
import com.electroboys.lightsnap.data.entity.SettingsConstants

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences(SettingsConstants.PREF_NAME, Context.MODE_PRIVATE)

    fun getScreenshotEnabled() = prefs.getBoolean(SettingsConstants.KEY_SCREENSHOT_ENABLED, false)
    fun getShortcutKey() = prefs.getString(SettingsConstants.KEY_SHORTCUT, SettingsConstants.DEFAULT_SHORTCUT) ?: SettingsConstants.DEFAULT_SHORTCUT
    fun getSavePath() = prefs.getString(SettingsConstants.KEY_SAVE_URI, "") ?: ""
    fun getCleanupOption() = prefs.getString(SettingsConstants.KEY_CLEANUP, SettingsConstants.DEFAULT_CLEANUP) ?: SettingsConstants.DEFAULT_CLEANUP
    fun getCleanupDeadline() = prefs.getInt(SettingsConstants.KEY_CLEANUP_DEADLINE, 0)

    fun setScreenshotEnabled(value: Boolean) = prefs.edit().putBoolean(SettingsConstants.KEY_SCREENSHOT_ENABLED, value).apply()
    fun setShortcutKey(key: String) = prefs.edit().putString(SettingsConstants.KEY_SHORTCUT, key).apply()
    fun setSavePath(path: String) = prefs.edit().putString(SettingsConstants.KEY_SAVE_URI, path).apply()
    fun setCleanupOption(option: String) = prefs.edit().putString(SettingsConstants.KEY_CLEANUP, option).apply()
    fun setCleanupDeadline(days: Int) = prefs.edit().putInt(SettingsConstants.KEY_CLEANUP_DEADLINE, days).apply()
}