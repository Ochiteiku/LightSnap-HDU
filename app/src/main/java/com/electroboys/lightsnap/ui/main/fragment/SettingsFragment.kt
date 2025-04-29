package com.electroboys.lightsnap.ui.main.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.electroboys.lightsnap.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.electroboys.lightsnap.ui.main.activity.VideoPlayActivity
import com.google.android.material.switchmaterial.SwitchMaterial


class SettingsFragment : Fragment(R.layout.fragment_settings){

    private lateinit var switchScreenshot: SwitchMaterial
    private lateinit var buttonReset: Button
    private lateinit var buttonVideoTest: Button

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

        // 从 SharedPreferences 获取值来设置开关状态
        val sharedPreferences = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val screenshotEnabled = sharedPreferences.getBoolean("screenshot_enabled", false)
        switchScreenshot.isChecked = screenshotEnabled

        setupUI()
    }

    private fun setupUI() {
        switchScreenshot.setOnCheckedChangeListener { _, isChecked ->
            val sharedPreferences = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("screenshot_enabled", isChecked)
            editor.apply()
        }

        buttonReset.setOnClickListener {
            val sharedPreferences = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
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
    }
}