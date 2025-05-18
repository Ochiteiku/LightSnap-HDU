package com.electroboys.lightsnap.utils

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.text.method.ScrollingMovementMethod
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.toColorInt
import com.electroboys.lightsnap.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object SummaryDialogUtils {
    fun showSummaryDialog(context: Context, summary: String) {
        val scrollView = ScrollView(context).apply {
            setPadding(dp2px(context, 16), dp2px(context, 12), dp2px(context, 16), dp2px(context, 12))
            setBackgroundResource(R.drawable.bg_summary_dialog) // 自定义圆角背景
        }

        val textView = TextView(context).apply {
            text = summary
            textSize = 16f
            setTextColor("#333333".toColorInt())
            setTextIsSelectable(true)
            movementMethod = ScrollingMovementMethod.getInstance()
            isFocusable = true
            isFocusableInTouchMode = true
        }

        val customTitle = TextView(context).apply {
            text = "📄 内容摘要"
            textSize = 20f
            setTextColor("#222222".toColorInt())
            setPadding(
                dp2px(context, 16), dp2px(context, 12), dp2px(context, 16), dp2px(context, 10)
            )
        }

        scrollView.addView(textView)
        MaterialAlertDialogBuilder(context)
            .setCustomTitle(customTitle)
            .setView(scrollView)
            .setPositiveButton("复制") { _, _ ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("摘要内容", summary)
                clipboard.setPrimaryClip(clipData)
                Toast.makeText(context, "摘要已复制", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("关闭", null)
            .show()
    }

    fun dp2px(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}
