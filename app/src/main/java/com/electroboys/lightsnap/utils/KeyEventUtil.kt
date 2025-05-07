package com.electroboys.lightsnap.utils

import android.view.KeyEvent

object KeyEventUtil {

    fun getPressedModifiers(event: KeyEvent): List<String> {
        val mods = mutableListOf<String>()
        if (event.isCtrlPressed) mods.add("Ctrl")
        if (event.isShiftPressed) mods.add("Shift")
        if (event.isAltPressed) mods.add("Alt")
        if (event.isMetaPressed) mods.add("Meta")
        return mods
    }

    fun isModifierKey(keyCode: Int): Boolean {
        return keyCode == KeyEvent.KEYCODE_CTRL_LEFT ||
                keyCode == KeyEvent.KEYCODE_CTRL_RIGHT ||
                keyCode == KeyEvent.KEYCODE_SHIFT_LEFT ||
                keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT ||
                keyCode == KeyEvent.KEYCODE_ALT_LEFT ||
                keyCode == KeyEvent.KEYCODE_ALT_RIGHT ||
                keyCode == KeyEvent.KEYCODE_META_LEFT ||
                keyCode == KeyEvent.KEYCODE_META_RIGHT
    }

    //捕获到的单键名称会带KEYCODE，将KEYCODE删掉
    fun singleKeyChange(keyName: String):String{
        return if (keyName.startsWith("KEYCODE_")) {
            keyName.removePrefix("KEYCODE_")
        } else {
            keyName
        }
    }

    fun cleanKeyName(rawKeyName: String): String {
        return rawKeyName.removePrefix("KEYCODE_")
    }



    //适用于MainActivity中的监听快捷键事件，expectedShortcut为从SharePreferences中获取的自定义快捷键
    fun matchShortcut(event: KeyEvent, expectedShortcut: String): Boolean {
        val currentModifiers = getPressedModifiers(event) //["Ctrl", "Shift"]
        val currentKey = cleanKeyName(KeyEvent.keyCodeToString(event.keyCode)) // "A"

        val expectedParts = expectedShortcut.split(" + ").map { it.trim() }

        val expectedKey = expectedParts.lastOrNull() ?: return false
        val expectedModifiers = expectedParts.dropLast(1)

        // 比较主键
        if (!currentKey.equals(expectedKey, ignoreCase = true)) return false

        // 比较修饰键（忽略顺序）
        return currentModifiers.toSet() == expectedModifiers.toSet()
    }
}