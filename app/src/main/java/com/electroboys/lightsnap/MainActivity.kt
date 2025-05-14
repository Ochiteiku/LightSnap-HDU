package com.electroboys.lightsnap

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.electroboys.lightsnap.data.entity.SettingsConstants
import com.electroboys.lightsnap.service.ScreenshotCleanupService
import com.electroboys.lightsnap.ui.main.activity.BaseActivity.BaseActivity
import com.electroboys.lightsnap.ui.main.fragment.DocumentDetailFragment
import com.electroboys.lightsnap.ui.main.fragment.DocumentFragment
import com.electroboys.lightsnap.ui.main.fragment.LibraryFragment
import com.electroboys.lightsnap.ui.main.fragment.MessageFragment
import com.electroboys.lightsnap.ui.main.fragment.SettingsFragment
import com.electroboys.lightsnap.utils.COSUtil
import com.electroboys.lightsnap.utils.SecretUtil


class MainActivity : BaseActivity() {

    // 缓存 Fragment 实例
    // （原先逻辑是每次切换选项卡都会新建 Fragment，把他们缓存为一个实例，这样切换的时候不会丢失页面信息）
    private val fragmentCache = mutableMapOf<Class<*>, Fragment>().apply {
        // 提前初始化所有 Fragment
        put(MessageFragment::class.java, MessageFragment())
        put(DocumentFragment::class.java, DocumentFragment())
        put(SettingsFragment::class.java, SettingsFragment())
        put(LibraryFragment::class.java, LibraryFragment())
    }

    private lateinit var navMessage: View
    private lateinit var navDocument: View
    private lateinit var navSettings: View
    private lateinit var navLibrary: View
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 初始化按钮
        navMessage = findViewById(R.id.navMessage)
        navDocument = findViewById(R.id.navDocument)
        navSettings = findViewById(R.id.navSettings)
        navLibrary = findViewById(R.id.navLibrary)

        sharedPreferences = getSharedPreferences(SettingsConstants.PREF_NAME, MODE_PRIVATE)

        //初始化COS
        var cleanOption = sharedPreferences.getString(SettingsConstants.KEY_CLEANUP,
            SettingsConstants.CLEANUP_OFF)
        if(cleanOption == SettingsConstants.CLEANUP_DELANDUPLOAD){
            if(COSUtil.initCOS(
                    context = this,
                    secretId = "AKIDfDlJiCE9tTDdptvNhpqhSI0VnsjeXK0Z",
                    secretKey = "hR2Tm25vkU2PYYTLAsRxMpGWbTBi1LQU",
                    region = "ap-shanghai",
                    bucket = "lightsnap-1318767045"
                )){
                startCleanupService()
            }else{
                Toast.makeText(this, "COS初始化失败", Toast.LENGTH_SHORT).show()
            }
        }else if(cleanOption==SettingsConstants.CLEANUP_DEL){
            startCleanupService()
        }

        // 默认显示 MessageFragment
        replaceFragment(MessageFragment::class.java)
        highlightNavItem(navMessage)

        // 点击事件
        // 设置点击监听
        navMessage.setOnClickListener {
            if(MessageFragment.getMessageSecret()){
                SecretUtil.setSecret(true)
            }else{
                SecretUtil.setSecret(false)
            }
            replaceFragment(MessageFragment::class.java)
            highlightNavItem(navMessage)
        }
        navDocument.setOnClickListener {
            if(DocumentDetailFragment.getDocumentSecret()){
                SecretUtil.setSecret(true)
            }else{
                SecretUtil.setSecret(false)
            }
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


    // 切换 Fragment 导航栏
    private fun replaceFragment(fragmentClass: Class<out Fragment>) {
        supportFragmentManager.beginTransaction().apply {
            // 隐藏所有已添加的 Fragment
            fragmentCache.values.forEach { fragment ->
                if (fragment.isAdded) {
                    hide(fragment)
                }
            }

            // 获取目标 Fragment（如果未添加则先添加）
            val targetFragment = fragmentCache[fragmentClass]!!
            if (!targetFragment.isAdded) {
                add(R.id.contentFrame, targetFragment)
            }

            // 显示目标 Fragment
            show(targetFragment)
            commit()
        }
    }


    private fun highlightNavItem(selectedView: View) {
        // 先全部设为未选中背景
        navMessage.setBackgroundResource(R.drawable.bg_nav_normal)
        navDocument.setBackgroundResource(R.drawable.bg_nav_normal)
        navSettings.setBackgroundResource(R.drawable.bg_nav_normal)
        navLibrary.setBackgroundResource(R.drawable.bg_nav_normal)

        // 给选中的那个设置选中背景
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

}
