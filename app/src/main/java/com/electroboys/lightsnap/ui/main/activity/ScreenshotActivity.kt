package com.electroboys.lightsnap.ui.main.activity

import android.graphics.Bitmap
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.electroboys.lightsnap.domain.screenshot.ScreenshotUtils
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

                    val bitmap = ScreenshotUtils.captureWithStatusBar(activity)
                    val selectedRect = selectionOverlayView.getSelectedRect()

                    val croppedBitmap = if (selectedRect != null && selectedRect.width() > 0 && selectedRect.height() > 0) {
                        Bitmap.createBitmap(bitmap, selectedRect.left, selectedRect.top, selectedRect.width(), selectedRect.height())
                    } else {
                        bitmap
                    }

                    onCaptureListener?.invoke(croppedBitmap)
                    isBoxSelectEnabled = false
                    selectionOverlayView.clearSelection()
                    container.removeView(selectionOverlayView)
                }
            }
            true
        }


        selectionOverlayView.setOnTouchListener(touchListener)
    }
}
