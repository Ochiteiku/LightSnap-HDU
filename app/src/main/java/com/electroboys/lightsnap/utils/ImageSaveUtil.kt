package com.electroboys.lightsnap.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.electroboys.lightsnap.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.IOException

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
                    Toast.makeText(context, "图片已保存为 $fileName.png", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "图片压缩失败", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "保存出错：${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // 带命名的保存操作
    fun saveBitmapWithName(
        context: Context,
        bitmap: Bitmap,
        treeUri: Uri,
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
        val btnUseDefault = view.findViewById<Button>(R.id.btnUseDefault)

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
            onResult?.invoke(false)
        }

        btnUseDefault.setOnClickListener {
            val defaultName = "screenshot_${System.currentTimeMillis()}"
            dialog.dismiss()
            saveBitmap(context, bitmap, treeUri, defaultName)
            onResult?.invoke(true)
        }

        dialog.show()
    }
}
