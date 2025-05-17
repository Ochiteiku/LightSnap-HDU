package com.electroboys.lightsnap.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import android.util.Log

object ScreenshotHelper {

    /**
     * 将Bitmap保存到缓存目录，并返回对应FileProvider Uri
     */
    fun saveBitmapToCacheAndGetUri(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs() // 创建目录

            // 生成临时文件名
            val file = File(cachePath, "image_${System.currentTimeMillis()}.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()

            // 通过FileProvider生成content Uri
            FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 复制图片Uri到剪贴板，便于粘贴
     */
    fun copyImageUriToClipboard(context: Context, imageUri: Uri) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // 复制前：获取剪贴板当前内容
        val beforeClip = clipboard.primaryClip
        Log.d(TAG, "复制前剪贴板内容：${clipDataToString(beforeClip)}")

        // 创建一个带有图片 Uri 的 ClipData
        val clip = ClipData.newUri(context.contentResolver, "Image", imageUri)
        clipboard.setPrimaryClip(clip)
        Log.d("TAG", "复制成功: $imageUri")

        // 复制后：再次获取剪贴板内容
        val afterClip = clipboard.primaryClip
        Log.d(TAG, "复制后剪贴板内容：${clipDataToString(afterClip)}")

        Log.d(TAG, "图片Uri已复制到剪贴板")
    }

    private fun clipDataToString(clipData: ClipData?): String {
        if (clipData == null) return "null"

        val sb = StringBuilder()
        for (i in 0 until clipData.itemCount) {
            val item = clipData.getItemAt(i)
            sb.append("Item $i: ")
            when {
                item.text != null -> sb.append("Text=${item.text}")
                item.uri != null -> sb.append("Uri=${item.uri}")
                item.intent != null -> sb.append("Intent=${item.intent}")
                else -> sb.append("Unknown item")
            }
            sb.append("\n")
        }
        return sb.toString()
    }

    /**
     * 一步操作：保存Bitmap并复制到剪贴板
     */
    fun copyBitmapToClipboard(context: Context, bitmap: Bitmap) {
        Log.d(TAG, "开始copyBitmapToClipboard")
        val uri = saveBitmapToCacheAndGetUri(context, bitmap)
        Log.d(TAG, "保存得到uri = $uri")
        uri?.let {
            copyImageUriToClipboard(context, it)
        }
    }
}
