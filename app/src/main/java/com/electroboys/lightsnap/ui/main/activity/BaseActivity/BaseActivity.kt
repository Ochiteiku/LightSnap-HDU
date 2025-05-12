package com.electroboys.lightsnap.ui.main.activity.BaseActivity

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.data.screenshot.BitmapCache
import com.electroboys.lightsnap.ui.main.activity.ScreenshotActivity
import com.electroboys.lightsnap.ui.main.activity.ScreenshotActivityForBase
import com.electroboys.lightsnap.ui.main.viewmodel.MainViewModel
import com.electroboys.lightsnap.utils.ImageSaveUtil
import com.electroboys.lightsnap.utils.KeyEventUtil
import com.electroboys.lightsnap.utils.SecretUtil

open class BaseActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var screenshotResultLauncher: ActivityResultLauncher<Intent>

    // 标志：是否处于截图模式
    private var isTakingScreenshot = false
    private var currentScreenshotHelper: ScreenshotActivityForBase? = null
    // 快捷键监听事件
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

        screenshotResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.d("BaseActivity", "接收到 onActivityResult")
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val bitmapKey = data?.getStringExtra("bitmap_key")
                Log.d("BaseActivity", "接收到 bitmapKey: $bitmapKey")

                if (bitmapKey != null) {
                    val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
                    val uriString = sharedPreferences.getString("screenshot_save_uri", null)

                    if (uriString != null) {
                        val treeUri = uriString.toUri()
                        val bitmap = BitmapCache.getBitmap(bitmapKey)
                        if (bitmap != null) {
                            ImageSaveUtil.saveBitmapWithName(this, bitmap, treeUri) { success ->
                                if (success) {
//                                    Toast.makeText(this, "图片已保存", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(this, "缓存图片不存在", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "未设置保存路径", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


    }

    private fun observeShortcutEvents() {
        viewModel.shortcutEvent.observe(this) { shortcut ->
            if (SecretUtil.isSecretMode()) {
                SecretUtil.showSecretToast(this)
                return@observe
            }
            val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
            val screenshotEnabled = prefs.getBoolean("screenshot_enabled", false)

            if (screenshotEnabled) {

                // 如果已经在截图模式，直接返回，防止重复进入
                if (isTakingScreenshot) return@observe

                isTakingScreenshot = true

                val screenshotHelper = ScreenshotActivityForBase(this)
                currentScreenshotHelper = screenshotHelper

                screenshotHelper.enableBoxSelectOnce { bitmap ->
                    runOnUiThread {
                        if (bitmap != null) {
                            val bitmapKey = BitmapCache.cacheBitmap(bitmap)
                            val intent = Intent(this, ScreenshotActivity::class.java).apply {
                                putExtra(ScreenshotActivity.EXTRA_SCREENSHOT_KEY, bitmapKey)
                            }

                            val options = ActivityOptions.makeCustomAnimation(
                                this,
                                R.anim.shot_enter, // 进入动画
                                R.anim.shot_exit   // 退出动画
                            )
                            screenshotResultLauncher.launch(intent)
                        } else {
                            Toast.makeText(this, "截图已取消", Toast.LENGTH_SHORT).show()
                        }

                        isTakingScreenshot = false
                        currentScreenshotHelper = null
                    }
                }

            } else {
                Toast.makeText(this, "灵截功能未启用", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 重写onBackPressed拦截返回键逻辑
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (isTakingScreenshot) {
            // 处于截图模式，仅取消截图，不清除页面
            Toast.makeText(this, "截图已取消", Toast.LENGTH_SHORT).show()
            currentScreenshotHelper?.let { helper ->
                val container = findViewById<FrameLayout>(android.R.id.content)
                helper.cleanup(container)
            }
            isTakingScreenshot = false
            currentScreenshotHelper = null
        } else {
            super.onBackPressed()
        }
    }
}
