package com.electroboys.lightsnap

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.electroboys.lightsnap.ui.main.fragment.DocumentFragment
import com.electroboys.lightsnap.ui.main.fragment.MessageFragment
import com.electroboys.lightsnap.ui.main.fragment.SettingsFragment
import com.electroboys.lightsnap.ui.main.fragment.LibraryFragment
import androidx.activity.viewModels
import com.electroboys.lightsnap.ui.main.activity.BaseActivity.BaseActivity
import com.electroboys.lightsnap.ui.main.viewmodel.MainViewModel


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
    private val viewModel: MainViewModel by viewModels()

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

        //初始化截图
//        screenshotHelper = ScreenshotActivity(this)

        // 默认显示 MessageFragment
        replaceFragment(MessageFragment::class.java)
        highlightNavItem(navMessage)

        // 点击事件
        // 设置点击监听
        navMessage.setOnClickListener {
            replaceFragment(MessageFragment::class.java)
            highlightNavItem(navMessage)
        }
        navDocument.setOnClickListener {
            replaceFragment(DocumentFragment::class.java)
            highlightNavItem(navDocument)
        }
        navSettings.setOnClickListener {
            replaceFragment(SettingsFragment::class.java)
            highlightNavItem(navSettings)
        }
        navLibrary.setOnClickListener {
            replaceFragment(LibraryFragment::class.java)
            highlightNavItem(navLibrary)
        }

//        // 监听 ViewModel 中的快捷键事件
//        viewModel.shortcutEvent.observe(this) { shortcut ->
//            // 获取保存的设置
//            val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
//            val screenshotEnabled = sharedPreferences.getBoolean("screenshot_enabled", false)
//            if(screenshotEnabled){
//                Toast.makeText(this, "监听到快捷键：$shortcut", Toast.LENGTH_SHORT).show()
//                screenshotHelper.enableBoxSelectOnce { bitmap ->
//                    if (bitmap != null) {
//                        Toast.makeText(this, "截图成功", Toast.LENGTH_SHORT).show()
//                        // 你可以在这里显示到某个 ImageView 或保存文件
//                    } else {
//                        Toast.makeText(this, "截图取消或失败", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }else{
//                Toast.makeText(this, "灵截功能未启用", Toast.LENGTH_SHORT).show()
//            }
//        }
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

}
