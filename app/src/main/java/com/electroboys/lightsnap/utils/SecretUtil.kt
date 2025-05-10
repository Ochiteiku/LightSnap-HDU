package com.electroboys.lightsnap.utils

import android.content.Context
import android.widget.Toast

object SecretUtil {
    private var isSecret = false

    fun setSecret(enabled: Boolean) {
        isSecret = enabled
    }

    fun isSecretMode() = isSecret

    fun showSecretToast(context: Context) {
        if (isSecret) {
            Toast.makeText(context, "当前处于密聊模式", Toast.LENGTH_SHORT).show()
        }
    }
}