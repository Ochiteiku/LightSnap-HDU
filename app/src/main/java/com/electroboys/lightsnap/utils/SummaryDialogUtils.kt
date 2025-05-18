package com.electroboys.lightsnap.utils

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.widget.EditText
import android.widget.Toast

object SummaryDialogUtils {
    fun showSummaryDialog(context: Context, summary: String) {
        val editText = EditText(context)
        editText.setText(summary)
        editText.setTextIsSelectable(true)
        editText.isFocusable = false
        editText.isClickable = false
        editText.setPadding(32, 32, 32, 32)
        editText.setBackgroundColor(Color.TRANSPARENT)

        AlertDialog.Builder(context)
            .setTitle("内容摘要")
            .setView(editText)
            .setPositiveButton("复制") { dialog, _ ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("摘要", summary)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "摘要已复制", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("关闭") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
