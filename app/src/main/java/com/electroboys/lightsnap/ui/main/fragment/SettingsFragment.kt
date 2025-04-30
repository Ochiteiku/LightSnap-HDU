package com.electroboys.lightsnap.ui.main.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import com.electroboys.lightsnap.R
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.electroboys.lightsnap.ui.main.activity.VideoPlayActivity
import com.electroboys.lightsnap.utils.KeyEventUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.core.net.toUri
import com.electroboys.lightsnap.utils.UriUtil


class SettingsFragment : Fragment(R.layout.fragment_settings){

    private lateinit var switchScreenshot: SwitchMaterial
    private lateinit var buttonReset: Button
    private lateinit var buttonVideoTest: Button

    //快捷键设置
    private lateinit var shortcutKeyContainer: View
    private lateinit var shortcutKeyDisplay: TextView

    //截图默认保存路径设置
    private lateinit var savePathDisplay: TextView
    private lateinit var savePathContainer: View
    private lateinit var folderPickerLauncher: androidx.activity.result.ActivityResultLauncher<Intent>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        switchScreenshot = view.findViewById(R.id.switch_screenshot)
        buttonReset = view.findViewById(R.id.button_reset)
        buttonVideoTest = view.findViewById(R.id.button_videoTest)
        shortcutKeyDisplay = view.findViewById<TextView>(R.id.shortcutKeyDisplay)
        shortcutKeyContainer = view.findViewById<View>(R.id.shortcutKeyContainer)
        savePathContainer = view.findViewById(R.id.savePathContainer)
        savePathDisplay = view.findViewById(R.id.savePathDisplay)

        // 从 SharedPreferences 获取值来设置开关状态
        val sharedPreferences = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val screenshotEnabled = sharedPreferences.getBoolean("screenshot_enabled", false)
        switchScreenshot.isChecked = screenshotEnabled

        // 显示已保存路径
        folderPickerLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult

                // 授权长期访问权限（关键）
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                // 保存 Uri.toString() 到 SharedPreferences
                val pathStr = uri.toString()
                val prefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
                prefs.edit().putString("screenshot_save_uri", pathStr).apply()

                // 显示路径（你可以自定义显示格式）
                savePathDisplay.text = UriUtil.getPathFromUri(requireContext(), uri) ?: pathStr
            }
        }

        setupUI()
    }

    private fun setupUI() {
        switchScreenshot.setOnCheckedChangeListener { _, isChecked ->
            val sharedPreferences =
                requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("screenshot_enabled", isChecked)
            editor.apply()
        }

        buttonReset.setOnClickListener {
            val sharedPreferences =
                requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("screenshot_enabled", false)
            editor.apply()

            switchScreenshot.isChecked = false // 界面同步重置
        }

        //视频播放测试
        buttonVideoTest.setOnClickListener {
            val intent = Intent(requireContext(), VideoPlayActivity::class.java)
            startActivity(intent)
        }

        // 每次界面显示时，读取保存的快捷键
        val sharedPreferences =
            requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val savedShortcut = sharedPreferences.getString("screenshot_shortcut", "未设置")

        shortcutKeyDisplay.text = savedShortcut ?: "未设置"

        // 点击进入设置模式
        shortcutKeyContainer.setOnClickListener {
            setupShortcutKey()
        }

        // 设置保存路径
        savePathContainer.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            folderPickerLauncher.launch(intent)
        }

        val savedUriStr = sharedPreferences.getString("screenshot_save_uri", null)
        if (savedUriStr != null) {
            val uri = savedUriStr.toUri()
            savePathDisplay.text = UriUtil.getPathFromUri(requireContext(), uri) ?: "已保存路径"
        } else {
            savePathDisplay.text = "未设置"
        }

    }

    private fun setupShortcutKey() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_listen_shortcut, null)
        bottomSheetDialog.setContentView(view)

        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.show()

        view.isFocusableInTouchMode = true
        view.requestFocus()

        view.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                val modifiers = KeyEventUtil.getPressedModifiers(event)
                val keyName = KeyEventUtil.singleKeyChange(KeyEvent.keyCodeToString(event.keyCode)) // 转换去掉KEYCODE_

                // 判断是不是修饰键本身（Ctrl、Shift、Alt等），单按修饰键不算有效快捷键
                if (KeyEventUtil.isModifierKey(keyCode)) {
                    // 仅按了Ctrl/Shift，不处理，继续等待
                    return@setOnKeyListener true
                }

                val fullShortcut = if (modifiers.isNotEmpty()) {
                    (modifiers + keyName).joinToString(" + ")
                } else {
                    keyName // 单键，比如 A、B、C
                }

                // 设置UI显示
                shortcutKeyDisplay.text = fullShortcut

                // 保存到本地
                val sharedPreferences = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
                sharedPreferences.edit().putString("screenshot_shortcut", fullShortcut).apply()

                bottomSheetDialog.dismiss() // 捕获到快捷键后关闭
                return@setOnKeyListener true
            }
            false
        }
    }
}