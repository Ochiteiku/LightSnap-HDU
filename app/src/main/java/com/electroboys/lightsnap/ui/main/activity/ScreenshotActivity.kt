package com.electroboys.lightsnap.ui.main.activity

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.electroboys.lightsnap.domain.screenshot.ScreenshotPreviewDialog
import com.electroboys.lightsnap.domain.screenshot.ScreenshotUtil
import com.electroboys.lightsnap.domain.screenshot.SelectView

class ScreenshotActivity(private val activity: AppCompatActivity) {

    private val selectionOverlayView = SelectView(activity)
    private val startTouch = PointF()
    private val endTouch = PointF()
    private var isBoxSelectEnabled = false
    private var onCaptureListener: ((Bitmap?) -> Unit)? = null

    fun enableBoxSelectOnce(onCapture: (Bitmap?) -> Unit) {
        this.onCaptureListener = onCapture
        if (isBoxSelectEnabled) return

        isBoxSelectEnabled = true
        selectionOverlayView.clearSelection()

        // 添加 overlay 到根布局
        val container = activity.findViewById<FrameLayout>(android.R.id.content)
        if (selectionOverlayView.parent == null) {
            container.addView(selectionOverlayView)
        }

        Toast.makeText(activity, "按住屏幕进行拖动框选区域", Toast.LENGTH_SHORT).show()

        val touchListener = View.OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startTouch.set(event.x, event.y)
                    endTouch.set(event.x, event.y)
                }
                MotionEvent.ACTION_MOVE -> {
                    endTouch.set(event.x, event.y)
                    selectionOverlayView.setSelection(startTouch, endTouch)
                }
                MotionEvent.ACTION_UP -> {
                    selectionOverlayView.setSelection(startTouch, endTouch)

                    if (startTouch == endTouch) {
                        v.performClick()
                        onCapture(null)
                        return@OnTouchListener true
                    }

                    val container = activity.findViewById<FrameLayout>(android.R.id.content)

                    // 移除 SelectView，防止红框被截进去
                    container.removeView(selectionOverlayView)

                    val bitmap = ScreenshotUtil.captureWithStatusBar(activity)
                    val selectedRect = selectionOverlayView.getSelectedRect()

                    container.addView(selectionOverlayView)

                    val croppedBitmap = if (selectedRect != null && selectedRect.width() > 0 && selectedRect.height() > 0) {
                        Bitmap.createBitmap(bitmap, selectedRect.left, selectedRect.top, selectedRect.width(), selectedRect.height())
                    } else {
                        bitmap
                    }

                    onCaptureListener?.invoke(croppedBitmap)


                    showPreviewDialog(croppedBitmap, container)
                    cleanup(container)
                }

            }
            true
        }

        selectionOverlayView.setOnTouchListener(touchListener)
    }

    private fun showPreviewDialog(bitmap: Bitmap?, container: FrameLayout) {
        if (bitmap == null) return

        val fragmentManager = activity.supportFragmentManager
        val previewDialog = ScreenshotPreviewDialog(bitmap)
        previewDialog.show(fragmentManager, "screenshot_preview")
    }

    private fun cleanup(container: FrameLayout) {
        selectionOverlayView.clearSelection()
        selectionOverlayView.setOnTouchListener(null)

        if (selectionOverlayView.parent != null) {
            container.removeView(selectionOverlayView)
        }

        isBoxSelectEnabled = false
        onCaptureListener = null
    }


}
