package com.electroboys.lightsnap

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.electroboys.lightsnap.ui.main.fragment.DocumentFragment
import com.electroboys.lightsnap.ui.main.fragment.MessageFragment
import com.electroboys.lightsnap.ui.main.fragment.SettingsFragment
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.electroboys.lightsnap.ui.main.viewmodel.MainViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var navMessage: View
    private lateinit var navDocument: View
    private lateinit var navSettings: View
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

        // 默认加载消息Fragment，并高亮
        replaceFragment(MessageFragment())
        highlightNavItem(navMessage)

        // 点击事件
        navMessage.setOnClickListener {
            replaceFragment(MessageFragment())
            highlightNavItem(navMessage)
        }
        navDocument.setOnClickListener {
            replaceFragment(DocumentFragment())
            highlightNavItem(navDocument)
        }
        navSettings.setOnClickListener {
            replaceFragment(SettingsFragment())
            highlightNavItem(navSettings)
        }

        // 监听 ViewModel 中的快捷键事件
        viewModel.shortcutEvent.observe(this, Observer { shortcut ->
            // 获取保存的设置
            val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
            val screenshotEnabled = sharedPreferences.getBoolean("screenshot_enabled", false)
            if(screenshotEnabled){
                Toast.makeText(this, "监听到快捷键：$shortcut", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "灵截功能未启用", Toast.LENGTH_SHORT).show()
            }
        })
    }


    //切换Fragment 导航栏
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.contentFrame, fragment)
            .commit()
    }

    private fun highlightNavItem(selectedView: View) {
        // 先全部设为未选中背景
        navMessage.setBackgroundResource(R.drawable.bg_nav_normal)
        navDocument.setBackgroundResource(R.drawable.bg_nav_normal)
        navSettings.setBackgroundResource(R.drawable.bg_nav_normal)

        // 给选中的那个设置选中背景
        selectedView.setBackgroundResource(R.drawable.bg_nav_selected)
    }


    //快捷键监听事件
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (event.isShiftPressed && event.keyCode == KeyEvent.KEYCODE_A) {
                // 通知 ViewModel 快捷键按下了
                viewModel.onShortcutPressed("Shift + A")
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

}
