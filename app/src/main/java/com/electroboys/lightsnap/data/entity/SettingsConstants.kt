package com.electroboys.lightsnap.data.entity

object SettingsConstants {

    //SharePreferences Key
    const val PREF_NAME = "settings"
    const val KEY_SCREENSHOT_ENABLED = "screenshot_enabled"
    const val KEY_SHORTCUT = "screenshot_shortcut"
    const val KEY_SAVE_URI = "screenshot_save_uri"
    const val KEY_CLEANUP = "cleanup"
    const val KEY_CLEANUP_DEADLINE = "cleanup_deadline"

    //默认值
    const val DEFAULT_SHORTCUT = "Ctrl+Shift+A"
    const val DEFAULT_CLEANUP = "不清理"

    //清理相关
    val CLEANUP_OPTIONS = arrayOf("不清理", "定时删除", "定时上传至云存储")
    val CLEANUP_OFF = CLEANUP_OPTIONS[0]
    val CLEANUP_DEL = CLEANUP_OPTIONS[1]
    val CLEANUP_DELANDUPLOAD = CLEANUP_OPTIONS[2]
    val CLEANUP_TIME_OPTIONS = arrayOf("超过 1 天", "超过 3 天", "超过 7 天", "超过 14 天", "超过 30 天")
    val CLEANUP_VALUES = intArrayOf(1, 3, 7, 14, 30)
}