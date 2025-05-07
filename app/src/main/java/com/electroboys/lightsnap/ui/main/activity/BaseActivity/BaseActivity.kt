package com.electroboys.lightsnap.ui.main.activity.BaseActivity

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.domain.screenshot.BitmapCache
import com.electroboys.lightsnap.domain.screenshot.ScreenshotUtil
import com.electroboys.lightsnap.ui.main.activity.ScreenshotActivityRealDo
import com.electroboys.lightsnap.ui.main.viewmodel.MainViewModel
import com.electroboys.lightsnap.utils.KeyEventUtil
import kotlin.getValue

open class BaseActivity: AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

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
        super.onCreate(savedInstanceState)
        observeShortcutEvents()
    }

    private fun observeShortcutEvents() {
        viewModel.shortcutEvent.observe(this) { shortcut ->
            val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
            val screenshotEnabled = prefs.getBoolean("screenshot_enabled", false)
            if (screenshotEnabled) {
                Toast.makeText(this, "监听到快捷键：$shortcut", Toast.LENGTH_SHORT).show()
                val bitmap = ScreenshotUtil.captureWithStatusBar(this)
                val bitmapKey = BitmapCache.cacheBitmap(bitmap)

                val intent = Intent(this, ScreenshotActivityRealDo::class.java).apply {
                    putExtra(ScreenshotActivityRealDo.EXTRA_SCREENSHOT_KEY, bitmapKey)
                }

                //设定动画过渡用
                val options = ActivityOptions.makeCustomAnimation(
                    this,
                    R.anim.shot_enter, // 进入动画
                    R.anim.shot_exit  // 退出动画
                )
                startActivity(intent,options.toBundle())


            } else {
                Toast.makeText(this, "灵截功能未启用", Toast.LENGTH_SHORT).show()
            }
        }
    }
}