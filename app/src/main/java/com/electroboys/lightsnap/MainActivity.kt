package com.electroboys.lightsnap

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.electroboys.lightsnap.data.entity.SettingsConstants
import com.electroboys.lightsnap.domain.settings.SettingsRepository
import com.electroboys.lightsnap.service.ScreenshotCleanupService
import com.electroboys.lightsnap.ui.main.activity.BaseActivity.BaseActivity
import com.electroboys.lightsnap.ui.main.fragment.DocumentDetailFragment
import com.electroboys.lightsnap.ui.main.fragment.DocumentFragment
import com.electroboys.lightsnap.ui.main.fragment.LibraryFragment
import com.electroboys.lightsnap.ui.main.fragment.MessageFragment
import com.electroboys.lightsnap.ui.main.fragment.SettingsFragment
import com.electroboys.lightsnap.ui.main.viewmodel.SettingsViewModel
import com.electroboys.lightsnap.ui.main.viewmodel.factory.SettingsViewModelFactory
import com.electroboys.lightsnap.utils.COSUtil
import com.electroboys.lightsnap.utils.KeyUtil
import com.electroboys.lightsnap.utils.SecretUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : BaseActivity() {

    private val fragmentCache = mutableMapOf<Class<out Fragment>, Fragment>()
    private var currentFragment: Fragment? = null

    private lateinit var navMessage: View
    private lateinit var navDocument: View
    private lateinit var navSettings: View
    private lateinit var navLibrary: View
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 初始化配置类获取信息
        KeyUtil.initialize(applicationContext)

        navMessage = findViewById(R.id.navMessage)
        navDocument = findViewById(R.id.navDocument)
        navSettings = findViewById(R.id.navSettings)
        navLibrary = findViewById(R.id.navLibrary)

        sharedPreferences = getSharedPreferences(SettingsConstants.PREF_NAME, MODE_PRIVATE)

        val repository = SettingsRepository(applicationContext)
        viewModel = ViewModelProvider(this, SettingsViewModelFactory(repository))[SettingsViewModel::class.java]

        checkAndRequestSavePath()

        lifecycleScope.launch {
            val cleanOption = sharedPreferences.getString(
                SettingsConstants.KEY_CLEANUP,
                SettingsConstants.CLEANUP_OFF
            )
            if (cleanOption == SettingsConstants.CLEANUP_DELANDUPLOAD) {
                val success = withContext(Dispatchers.IO) {
                    COSUtil.initCOS(
                        context = this@MainActivity,
                        secretId = KeyUtil.getQCloudSecretId(),
                        secretKey = KeyUtil.getQCloudSecretKey(),
                        region = KeyUtil.getQCloudRegion(),
                        bucket = KeyUtil.getQCloudBucket(),
                    )
                }
                if (success) {
                    startCleanupService()
                } else {
                    Toast.makeText(this@MainActivity, "COS初始化失败", Toast.LENGTH_SHORT).show()
                }
            } else if (cleanOption == SettingsConstants.CLEANUP_DEL) {
                startCleanupService()
            }
        }

        // 默认显示 MessageFragment
        replaceFragment(MessageFragment::class.java)
        highlightNavItem(navMessage)

        navMessage.setOnClickListener {
            SecretUtil.setSecret(MessageFragment.getMessageSecret())
            replaceFragment(MessageFragment::class.java)
            highlightNavItem(navMessage)
        }

        navDocument.setOnClickListener {
            SecretUtil.setSecret(DocumentDetailFragment.getDocumentSecret())
            replaceFragment(DocumentFragment::class.java)
            highlightNavItem(navDocument)
        }

        navSettings.setOnClickListener {
            SecretUtil.setSecret(false)
            replaceFragment(SettingsFragment::class.java)
            highlightNavItem(navSettings)
        }

        navLibrary.setOnClickListener {
            SecretUtil.setSecret(false)
            replaceFragment(LibraryFragment::class.java)
            highlightNavItem(navLibrary)
        }
    }

    private fun replaceFragment(fragmentClass: Class<out Fragment>) {
        // 清除当前焦点并隐藏键盘，防止切换fragment时焦点不重置
        val currentFocusView = currentFocus
        currentFocusView?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }

        val transaction = supportFragmentManager.beginTransaction()

        val targetFragment = fragmentCache.getOrPut(fragmentClass) {
            fragmentClass.newInstance()
        }
        if (currentFragment === targetFragment) return

        //隐藏当前
        currentFragment?.let {
            if (it.isAdded) transaction.hide(it)
        }

        if (!targetFragment.isAdded) {
            transaction.add(R.id.contentFrame, targetFragment)
        }
        transaction.show(targetFragment).commit()

        currentFragment = targetFragment
    }

    private fun highlightNavItem(selectedView: View) {
        navMessage.setBackgroundResource(R.drawable.bg_nav_normal)
        navDocument.setBackgroundResource(R.drawable.bg_nav_normal)
        navSettings.setBackgroundResource(R.drawable.bg_nav_normal)
        navLibrary.setBackgroundResource(R.drawable.bg_nav_normal)
        selectedView.setBackgroundResource(R.drawable.bg_nav_selected)
    }

    private fun startCleanupService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(permission), 1001)
                return
            }
        } else {
            val areEnabled = NotificationManagerCompat.from(this).areNotificationsEnabled()
            if (!areEnabled) {
                Toast.makeText(this, "通知已被禁用，可能影响部分功能显示", Toast.LENGTH_LONG).show()
                return
            }
        }
        ScreenshotCleanupService.startService(this)
    }

    private fun checkAndRequestSavePath() {
        val path = viewModel.savePath.value ?: viewModel.repository.getSavePath()
        if (path.isNotBlank()) return

        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val defaultDir = File(picturesDir, SettingsConstants.DEFAULT_FOLDER_NAME)
        if (!defaultDir.exists()) defaultDir.mkdirs()

        val defaultPath = defaultDir.absolutePath
        viewModel.setSavePath(defaultPath)
    }
}
