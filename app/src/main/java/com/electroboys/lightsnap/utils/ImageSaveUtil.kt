package com.electroboys.lightsnap.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import com.electroboys.lightsnap.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageSaveUtil {
    // 使用路径和文件名保存 Bitmap 到 DocumentFile
    private fun saveBitmap(
        context: Context,
        bitmap: Bitmap,
        treeUri: Uri,
        fileName: String
    ) {
        try {
            val documentFile = DocumentFile.fromTreeUri(context, treeUri)
            val newFile = documentFile?.createFile("image/png", "$fileName.png")
                ?: throw IOException("无法创建文件")

            context.contentResolver.openOutputStream(newFile.uri, "w")?.use { outputStream ->
                if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
//                    Toast.makeText(context, "图片已保存为 $fileName.png", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "图片保存失败", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "保存出错：${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // 带命名的保存操作
    @SuppressLint("SetTextI18n")
    fun saveBitmapWithName(
        context: Context,
        bitmap: Bitmap,
        treeUri: Uri,
        onResult: ((success: Boolean) -> Unit)? = null
    ) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val defaultName = "Img_$timestamp"
        var isSaved = false

        // 创建提示弹窗
        val dialog = BottomSheetDialog(context)
        val messageView = LayoutInflater.from(context).inflate(R.layout.dialog_simple_message, null)
        dialog.setContentView(messageView)

        // 设置默认展开高度
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
                behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
            }
        }

        val messageText = messageView.findViewById<TextView>(R.id.messageText)
        messageText.text = "图片已保存为 $defaultName，点击进行自定义命名"

        // 点击弹窗时，关闭当前弹窗并打开命名弹窗
        messageText.setOnClickListener {
            dialog.dismiss()
            showRenameDialog(context, bitmap, treeUri, defaultName) { success ->
                isSaved = true // 已经在 rename dialog 中保存过了
                onResult?.invoke(success)
            }
        }

        dialog.show()

        // 设置3秒后自动执行默认保存
        messageView.postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
                onResult?.invoke(true)
            }
        }, 3000)

        dialog.setOnDismissListener {
            if (!isSaved) {
                saveBitmap(context, bitmap, treeUri, defaultName)
            }
            onResult?.invoke(true)
        }
    }

    private fun showRenameDialog(
        context: Context,
        bitmap: Bitmap,
        treeUri: Uri,
        defaultName: String,
        onResult: ((success: Boolean) -> Unit)? = null
    ) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_filename, null)
        dialog.setContentView(view)

        // 设置默认展开高度
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
                behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
            }
        }

        val etFileName = view.findViewById<EditText>(R.id.etFileName)
        val btnConfirm = view.findViewById<Button>(R.id.btnConfirm)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnClear = view.findViewById<Button>(R.id.btnClear)

        btnConfirm.setOnClickListener {
            val input = etFileName.text.toString().trim()
            if (input.isEmpty()) {
                Toast.makeText(context, "文件名不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dialog.dismiss()
            saveBitmap(context, bitmap, treeUri, input)
            onResult?.invoke(true)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
            saveBitmap(context, bitmap, treeUri, defaultName)
            onResult?.invoke(false)
        }

        btnClear.setOnClickListener {
            etFileName.text.clear()
        }

        dialog.show()

        dialog.setOnDismissListener {
            Toast.makeText(context, "图片已保存", Toast.LENGTH_SHORT).show()
        }
    }


}

