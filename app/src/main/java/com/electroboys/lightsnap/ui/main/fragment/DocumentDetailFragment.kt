package com.electroboys.lightsnap.ui.main.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.DialogFragment.STYLE_NO_TITLE
import androidx.fragment.app.Fragment
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.data.entity.Document

// 全屏状态标志
private var isFullscreen = false

class DocumentDetailFragment : Fragment(R.layout.doc_document_detail) {

    companion object {
        fun newInstance(document: Document) = DocumentDetailFragment().apply {
            arguments = Bundle().apply { putParcelable("document", document) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view,savedInstanceState)
        val document = arguments?.getParcelable<Document>("document")!!

        view.findViewById<TextView>(R.id.detailTitle).text = document.title
        view.findViewById<TextView>(R.id.detailContent).text = document.content
        view.findViewById<TextView>(R.id.detailTime).text = "创建时间: ${document.time}"

        // 退出按键
        view.findViewById<ImageView>(R.id.closeButton).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 全屏按键
        view.findViewById<ImageView>(R.id.fcButton).setOnClickListener {
            var screen = R.id.main
            if (isFullscreen){
                screen = R.id.documentContainer
            }

            parentFragmentManager.popBackStack()
            val detailFragment = newInstance(document)
            parentFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(screen, detailFragment)
                .addToBackStack(null)
                .commit()

            isFullscreen = !isFullscreen
        }

        // 密聊
        val secretImage = view.findViewById<ImageView>(R.id.secretImage)
        val secretSwitch = view.findViewById<SwitchCompat>(R.id.secretSwitch)
        secretSwitch.setOnCheckedChangeListener { _, isChecked ->
            secretImage.setImageResource(
                if (isChecked) {
                    R.drawable.ic_eye_closed
                }
                else {
                    R.drawable.ic_eye_open
                }
            )
        }

        enterFullscreen()
    }

    private fun enterFullscreen() {
        activity?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false) // 允许内容延伸到系统栏区域
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.hide(WindowInsetsCompat.Type.systemBars()) // 隐藏状态栏和导航栏
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE // 临时显示系统栏
        }
    }

    override fun onDestroyView() {
        exitFullscreen()
        super.onDestroyView()
    }

    private fun exitFullscreen() {
        activity?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, true) // 恢复系统栏占位
            WindowCompat.getInsetsController(window, window.decorView).apply {
                show(WindowInsetsCompat.Type.systemBars()) // 显示状态栏和导航栏
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT // 恢复默认交互行为
            }
        }
    }
}