package com.electroboys.lightsnap.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.graphics.createBitmap

object ScreenshotUtil {

    fun captureWithStatusBar(activity: Activity): Bitmap {
        // 获取视图
        val view = activity.window.decorView

        // 创建一个与视图大小相同的 Bitmap
        val bitmap = createBitmap(view.width, view.height)

        // 使用 Canvas 将视图绘制到 Bitmap 上
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap
    }


    @SuppressLint("DiscouragedApi", "InternalInsetResource")
    fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else 0
    }
}