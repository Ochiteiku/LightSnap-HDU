package com.electroboys.lightsnap.ui.main.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.ui.main.activity.VideoPlayActivity
import com.electroboys.lightsnap.ui.main.activity.VideoPlayActivity2
import com.electroboys.lightsnap.utils.KeyEventUtil
import com.electroboys.lightsnap.utils.UriUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.switchmaterial.SwitchMaterial


class SettingsFragment : Fragment(R.layout.fragment_settings){

    private lateinit var switchScreenshot: SwitchMaterial
    private lateinit var buttonReset: Button
    private lateinit var buttonVideoTest: Button
    private lateinit var buttonVideoTest2: Button

    //快捷键设置
    private lateinit var shortcutKeyContainer: View
    private lateinit var shortcutKeyDisplay: TextView

    //截图默认保存路径设置
    private lateinit var savePathDisplay: TextView
    private lateinit var savePathContainer: View
    private lateinit var folderPickerLauncher: androidx.activity.result.ActivityResultLauncher<Intent>

    //清理相关设置
    private lateinit var autoCleanContainer: View
    private lateinit var autoClean: TextView
    private lateinit var deadLineDisplay: TextView
    private lateinit var deadLineContainer: View

    private val cleanupOptions = arrayOf("不清理", "定时删除" , "定时上传至云存储")
    private val cleanupTimeOptions = arrayOf("超过 1 天", "超过 3 天", "超过 7 天", "超过 14 天", "超过 30 天")
    private val cleanupValues = intArrayOf(1, 3, 7, 14, 30) // 对应逻辑上的天数，0 表示不清理


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
        buttonVideoTest2 = view.findViewById(R.id.button_videoTest2)
        shortcutKeyDisplay = view.findViewById<TextView>(R.id.shortcutKeyDisplay)
        shortcutKeyContainer = view.findViewById<View>(R.id.shortcutKeyContainer)
        savePathContainer = view.findViewById(R.id.savePathContainer)
        savePathDisplay = view.findViewById(R.id.savePathDisplay)
        deadLineDisplay = view.findViewById(R.id.deadLineDisplay)
        deadLineContainer = view.findViewById(R.id.deadlineContainer)
        autoCleanContainer = view.findViewById(R.id.autoCleanContainer)
        autoClean = view.findViewById(R.id.autoClean)


        setupUI()
    }

    //设置UI，并给某些UI组件设置点按监听
    private fun setupUI() {
        val sharedPreferences = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val screenshotEnabled = sharedPreferences.getBoolean("screenshot_enabled", false)
        val savedShortcut = sharedPreferences.getString("screenshot_shortcut", "Ctrl+Shift+A")
        val editor = sharedPreferences.edit()
        val savedDays = sharedPreferences.getInt("cleanup_deadline", 0)
        val cleanupOption = sharedPreferences.getString("cleanup","不清理")
        val initialIndex = cleanupValues.indexOf(savedDays).coerceAtLeast(0)

        //初始化已有参数
        switchScreenshot.isChecked = screenshotEnabled
        shortcutKeyDisplay.text = savedShortcut ?: "Ctrl+Shift+A"
        deadLineDisplay.text = cleanupTimeOptions[initialIndex]
        autoClean.text = cleanupOption ?:  "不清理"

        //如果设置为不清理，则无需设置时间
        val isDeadlineEnabled = autoClean.text != "不清理"
        deadLineContainer.isEnabled = isDeadlineEnabled
        deadLineDisplay.setTextColor(
            if (isDeadlineEnabled) Color.BLACK else Color.GRAY
        )

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
                editor.putString("screenshot_save_uri", pathStr).apply()

                // 显示路径（你可以自定义显示格式）
                savePathDisplay.text = UriUtil.getPathFromUri(requireContext(), uri) ?: pathStr
            }
        }
        val savedUriStr = sharedPreferences.getString("screenshot_save_uri", null)
        if (savedUriStr != null) {
            val uri = savedUriStr.toUri()
            savePathDisplay.text = UriUtil.getPathFromUri(requireContext(), uri) ?: "已保存路径"
        } else {
            savePathDisplay.text = "未设置"
        }

        switchScreenshot.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean("screenshot_enabled", isChecked)
            editor.apply()
        }

        buttonReset.setOnClickListener {
            editor.putBoolean("screenshot_enabled", false)
            editor.apply()

            switchScreenshot.isChecked = false // 界面同步重置
        }

        //视频播放测试
        buttonVideoTest.setOnClickListener {
            val intent = Intent(requireContext(), VideoPlayActivity::class.java)
            startActivity(intent)
        }
        //视频播放测试2
        buttonVideoTest2.setOnClickListener {
            val intent = Intent(requireContext(), VideoPlayActivity2::class.java)
            startActivity(intent)
        }


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

        //设置清理方式
        autoCleanContainer.setOnClickListener {
            setupCleanupOption(autoClean)
        }

        //设置清理时间
        deadLineContainer.setOnClickListener {
            setupCleanupTimeOption(deadLineDisplay)
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

    private fun setupCleanupTimeOption(textView: TextView){
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val currentDays = prefs.getInt("cleanup_deadline", 0)
        val currentIndex = cleanupValues.indexOf(currentDays).coerceAtLeast(0)

        AlertDialog.Builder(requireContext())
            .setTitle("清理截图周期")
            .setSingleChoiceItems(cleanupTimeOptions, currentIndex) { dialog, which ->
                val selectedDays = cleanupValues[which]
                prefs.edit().putInt("cleanup_deadline", selectedDays).apply()
                textView.text = cleanupTimeOptions[which]
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun setupCleanupOption(textView: TextView){
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val current = prefs.getString("cleanup","不清理")
        val currentIndex = cleanupOptions.indexOf(current).coerceAtLeast(0)

        AlertDialog.Builder(requireContext())
            .setTitle("清理截图方式")
            .setSingleChoiceItems(cleanupOptions, currentIndex) { dialog, which ->
                val selected = cleanupOptions[which]
                prefs.edit().putString("cleanup",selected).apply()
                textView.text = cleanupOptions[which]
                dialog.dismiss()
                // 控制 deadLineContainer 是否可用
                val isDeadlineEnabled = which != 0 // “不清理”对应 index = 0
                deadLineContainer.isEnabled = isDeadlineEnabled
                deadLineDisplay.setTextColor(
                    if (isDeadlineEnabled) Color.BLACK else Color.GRAY
                )
            }
            .setNegativeButton("取消", null)
            .show()
    }

}