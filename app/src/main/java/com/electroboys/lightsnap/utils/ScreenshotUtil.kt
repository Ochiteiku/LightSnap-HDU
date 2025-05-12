package com.electroboys.lightsnap.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.createBitmap
import java.util.concurrent.CountDownLatch

object ScreenshotUtil {

    @SuppressLint("RequiresApi")
    fun captureWithStatusBar(activity: Activity, onBitmapReady: (Bitmap) -> Unit) {
        val rootView = activity.window.decorView
        val width = rootView.width
        val height = rootView.height

        if (width == 0 || height == 0) {
            // 容错处理，避免宽高为0时崩溃
            onBitmapReady(createBitmap(1, 1))
            return
        }

        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        rootView.draw(canvas)

        // 找出所有 SurfaceView（包括嵌套的）
        val surfaceViews = mutableListOf<SurfaceView>()
        fun findSurfaceViews(view: View) {
            if (view is SurfaceView) surfaceViews.add(view)
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    findSurfaceViews(view.getChildAt(i))
                }
            }
        }
        findSurfaceViews(rootView)

        if (surfaceViews.isEmpty() || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            // 无 SurfaceView 或系统不支持 PixelCopy
            onBitmapReady(bitmap)
            return
        }

        // 用 PixelCopy 复制所有 SurfaceView 的内容到 bitmap 上
        val latch = CountDownLatch(surfaceViews.size)

        for (surfaceView in surfaceViews) {
            val surfaceBitmap = createBitmap(surfaceView.width, surfaceView.height)

            PixelCopy.request(
                surfaceView,
                surfaceBitmap,
                { result ->
                    if (result == PixelCopy.SUCCESS) {
                        // 合成该 SurfaceView 到原图
                        val location = IntArray(2)
                        surfaceView.getLocationOnScreen(location)
                        canvas.drawBitmap(surfaceBitmap, location[0].toFloat(), location[1].toFloat(), null)
                    }
                    latch.countDown()
                },
                Handler(Looper.getMainLooper())
            )
        }

        Thread {
            latch.await()
            onBitmapReady(bitmap)
        }.start()
    }

    @SuppressLint("DiscouragedApi", "InternalInsetResource")
    fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else 0
    }
}