package com.electroboys.lightsnap.ui.main.activity

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.PointF
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.electroboys.lightsnap.domain.screenshot.SelectView
import com.electroboys.lightsnap.utils.ScreenshotUtil

class ScreenshotActivityForBase(private val activity: AppCompatActivity) {

    private val selectionOverlayView = SelectView(activity)
    private val startTouch = PointF()
    private val endTouch = PointF()
    private var isBoxSelectEnabled = false
    private var onCaptureListener: ((Bitmap?) -> Unit)? = null
    private var maskView: View? = null// 灰幕

    @SuppressLint("ClickableViewAccessibility")
    fun enableBoxSelectOnce(onCapture: (Bitmap?) -> Unit) {
        this.onCaptureListener = onCapture
        if (isBoxSelectEnabled) return

        isBoxSelectEnabled = true
        selectionOverlayView.clearSelection()

        // 创建灰幕 View
        maskView = View(activity).apply {
            setBackgroundColor(0xAA000000.toInt()) // 半透明黑色
        }

        // 添加 overlay 到根布局
        val container = activity.findViewById<FrameLayout>(android.R.id.content)
        if (selectionOverlayView.parent == null) {
            container.addView(maskView) // 先添加灰幕
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
                    hideMask()
                    endTouch.set(event.x, event.y)
                    selectionOverlayView.setSelection(startTouch, endTouch)
                }
                MotionEvent.ACTION_UP -> {
                    selectionOverlayView.setSelection(startTouch, endTouch)

                    val container = activity.findViewById<FrameLayout>(android.R.id.content)

                    if (startTouch == endTouch) {
                        // 用户点击未拖动，表示取消截图
                        Toast.makeText(activity, "已取消截图", Toast.LENGTH_SHORT).show()

                        cleanup(container)
                        onCaptureListener?.invoke(null) // 明确回调 null 表示取消
                        return@OnTouchListener true
                    }

                    // 正常截图流程
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
                    cleanup(container)
                }

            }
            true
        }


        //先将该页面设为焦点，方便快捷键监控，然后监控空格键，空格键则直接全屏截图
        selectionOverlayView.isFocusableInTouchMode = true
        selectionOverlayView.requestFocus()

        selectionOverlayView.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_SPACE && event.action == KeyEvent.ACTION_DOWN) {
                val bitmap = ScreenshotUtil.captureWithStatusBar(activity)
                onCaptureListener?.invoke(bitmap)

                val container = activity.findViewById<FrameLayout>(android.R.id.content)
                cleanup(container)
                true
            } else {
                false
            }
        }

        selectionOverlayView.setOnTouchListener(touchListener)

    }


    private fun showMask() {
        maskView?.visibility = View.VISIBLE
    }

    private fun hideMask() {
        maskView?.visibility = View.INVISIBLE
    }

    fun cleanup(container: FrameLayout) {
        selectionOverlayView.clearSelection()
        selectionOverlayView.setOnTouchListener(null)

        if (selectionOverlayView.parent != null) {
            container.removeView(selectionOverlayView)
        }

        // 移除灰幕
        maskView?.let {
            container.removeView(it)
            maskView = null
        }

        isBoxSelectEnabled = false
        onCaptureListener = null
    }

}
