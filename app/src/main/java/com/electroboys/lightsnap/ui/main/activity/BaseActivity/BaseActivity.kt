package com.electroboys.lightsnap.ui.main.activity.BaseActivity

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.electroboys.lightsnap.ui.main.activity.ScreenshotActivity
import com.electroboys.lightsnap.ui.main.viewmodel.MainViewModel
import com.electroboys.lightsnap.utils.KeyEventUtil
import kotlin.getValue

open class BaseActivity: AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var screenshotHelper: ScreenshotActivity

    //快捷键监听事件
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
            val savedShortcut = prefs.getString("screenshot_shortcut", null)
            if (!savedShortcut.isNullOrEmpty()) {
                if (KeyEventUtil.matchShortcut(event, savedShortcut)) {
                    viewModel.onShortcutPressed(savedShortcut)
                    return true
                }
            }
        }

        return super.dispatchKeyEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        //初始化截图
        screenshotHelper = ScreenshotActivity(this)
        super.onCreate(savedInstanceState)
        observeShortcutEvents()
    }

    private fun observeShortcutEvents() {
        viewModel.shortcutEvent.observe(this) { shortcut ->
            val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
            val screenshotEnabled = prefs.getBoolean("screenshot_enabled", false)
            if (screenshotEnabled) {
                Toast.makeText(this, "监听到快捷键：$shortcut", Toast.LENGTH_SHORT).show()
                screenshotHelper.enableBoxSelectOnce { bitmap ->
                    if (bitmap != null) {
                        Toast.makeText(this, "截图成功", Toast.LENGTH_SHORT).show()
                        // 这里你可以保存或跳转
                    } else {
                        Toast.makeText(this, "截图取消或失败", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "灵截功能未启用", Toast.LENGTH_SHORT).show()
            }
        }
    }
}