package com.electroboys.lightsnap.ui.main.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.data.entity.SettingsConstants
import com.electroboys.lightsnap.domain.settings.SettingsRepository
import com.electroboys.lightsnap.ui.main.activity.VideoPlayActivity
import com.electroboys.lightsnap.ui.main.activity.VideoPlayActivity2
import com.electroboys.lightsnap.ui.main.viewmodel.SettingsViewModel
import com.electroboys.lightsnap.ui.main.viewmodel.factory.SettingsViewModelFactory
import com.electroboys.lightsnap.utils.KeyEventUtil
import com.electroboys.lightsnap.utils.UriUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.switchmaterial.SwitchMaterial


class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var viewModel: SettingsViewModel

    private lateinit var switchScreenshot: SwitchMaterial
    private lateinit var buttonReset: Button
    private lateinit var buttonVideoTest: Button
    private lateinit var buttonVideoTest2: Button

    private lateinit var shortcutKeyContainer: View
    private lateinit var shortcutKeyDisplay: TextView

    private lateinit var savePathDisplay: TextView
    private lateinit var savePathContainer: View
    private lateinit var folderPickerLauncher: ActivityResultLauncher<Intent>

    private lateinit var autoCleanContainer: View
    private lateinit var autoClean: TextView
    private lateinit var deadLineDisplay: TextView
    private lateinit var deadLineContainer: View

    private val cleanupOptions get() = SettingsConstants.CLEANUP_OPTIONS
    private val cleanupTimeOptions get() = SettingsConstants.CLEANUP_TIME_OPTIONS
    private val cleanupValues get() = SettingsConstants.CLEANUP_VALUES

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = SettingsRepository(requireContext())
        viewModel = ViewModelProvider(this, SettingsViewModelFactory(repository))[SettingsViewModel::class.java]

        switchScreenshot = view.findViewById(R.id.switch_screenshot)
        buttonReset = view.findViewById(R.id.button_reset)
        buttonVideoTest = view.findViewById(R.id.button_videoTest)
        buttonVideoTest2 = view.findViewById(R.id.button_videoTest2)
        shortcutKeyDisplay = view.findViewById(R.id.shortcutKeyDisplay)
        shortcutKeyContainer = view.findViewById(R.id.shortcutKeyContainer)
        savePathContainer = view.findViewById(R.id.savePathContainer)
        savePathDisplay = view.findViewById(R.id.savePathDisplay)
        deadLineDisplay = view.findViewById(R.id.deadLineDisplay)
        deadLineContainer = view.findViewById(R.id.deadlineContainer)
        autoCleanContainer = view.findViewById(R.id.autoCleanContainer)
        autoClean = view.findViewById(R.id.autoClean)

        setupObservers()
        setupListeners()

        folderPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                viewModel.setSavePath(uri.toString())
            }
        }
    }

    private fun setupObservers() {
        viewModel.isScreenshotEnabled.observe(viewLifecycleOwner) {
            switchScreenshot.isChecked = it
        }
        viewModel.shortcutKey.observe(viewLifecycleOwner) {
            shortcutKeyDisplay.text = it
        }
        viewModel.savePath.observe(viewLifecycleOwner) {
            val pathText = UriUtil.getPathFromUri(requireContext(), it.toUri()) ?: it
            savePathDisplay.text = pathText
        }
        viewModel.cleanupOption.observe(viewLifecycleOwner) {
            autoClean.text = it
            val isDeadlineEnabled = it != cleanupOptions[0] // 不清理 = 不启用
            deadLineContainer.isEnabled = isDeadlineEnabled
            deadLineDisplay.setTextColor(if (isDeadlineEnabled) Color.BLACK else Color.GRAY)
        }
        viewModel.cleanupDeadline.observe(viewLifecycleOwner) {
            val index = cleanupValues.indexOf(it).coerceAtLeast(0)
            deadLineDisplay.text = cleanupTimeOptions[index]
        }
    }

    private fun setupListeners() {
        switchScreenshot.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setScreenshotEnabled(isChecked)
        }

        buttonReset.setOnClickListener {
            viewModel.setScreenshotEnabled(false)
            viewModel.setShortcutKey(SettingsConstants.DEFAULT_SHORTCUT)
            viewModel.setCleanupOption(SettingsConstants.DEFAULT_CLEANUP)
            viewModel.setCleanupDeadline(0)
        }

        buttonVideoTest.setOnClickListener {
            startActivity(Intent(requireContext(), VideoPlayActivity::class.java))
        }

        buttonVideoTest2.setOnClickListener {
            startActivity(Intent(requireContext(), VideoPlayActivity2::class.java))
        }

        shortcutKeyContainer.setOnClickListener {
            setupShortcutKey()
        }

        savePathContainer.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            folderPickerLauncher.launch(intent)
        }

        autoCleanContainer.setOnClickListener {
            setupCleanupOption()
        }

        deadLineContainer.setOnClickListener {
            setupCleanupTimeOption()
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
                val keyName = KeyEventUtil.singleKeyChange(KeyEvent.keyCodeToString(keyCode))
                if (KeyEventUtil.isModifierKey(keyCode)) return@setOnKeyListener true
                val fullShortcut = if (modifiers.isNotEmpty()) {
                    (modifiers + keyName).joinToString(" + ")
                } else keyName
                viewModel.setShortcutKey(fullShortcut)
                bottomSheetDialog.dismiss()
                return@setOnKeyListener true
            }
            false
        }
    }

    private fun setupCleanupTimeOption() {
        val currentDays = viewModel.cleanupDeadline.value ?: 0
        val currentIndex = cleanupValues.indexOf(currentDays).coerceAtLeast(0)
        AlertDialog.Builder(requireContext())
            .setTitle("清理截图周期")
            .setSingleChoiceItems(cleanupTimeOptions, currentIndex) { dialog, which ->
                viewModel.setCleanupDeadline(cleanupValues[which])
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun setupCleanupOption() {
        val current = viewModel.cleanupOption.value ?: cleanupOptions[0]
        val currentIndex = cleanupOptions.indexOf(current).coerceAtLeast(0)
        AlertDialog.Builder(requireContext())
            .setTitle("清理截图方式")
            .setSingleChoiceItems(cleanupOptions, currentIndex) { dialog, which ->
                viewModel.setCleanupOption(cleanupOptions[which])
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }
}