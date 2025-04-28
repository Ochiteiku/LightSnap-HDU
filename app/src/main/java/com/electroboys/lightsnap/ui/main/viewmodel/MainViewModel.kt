package com.electroboys.lightsnap.ui.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MainViewModel: ViewModel() {


    // 用于通知UI层有快捷键被触发
    private val _shortcutEvent = MutableLiveData<String>()
    val shortcutEvent: LiveData<String> get() = _shortcutEvent

    // 调用此方法，传入快捷键信息
    fun onShortcutPressed(shortcut: String) {
        _shortcutEvent.value = shortcut
    }

}