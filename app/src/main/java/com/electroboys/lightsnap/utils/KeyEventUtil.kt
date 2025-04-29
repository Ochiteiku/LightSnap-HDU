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
}