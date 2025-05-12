package com.electroboys.lightsnap.ui.main.activity

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.PointF
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
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

        val fullScreenBitmap = ScreenshotUtil.captureWithStatusBar(activity)  // 提前截图

        // 创建灰幕 View
        maskView = View(activity).apply {
            setBackgroundColor(0xAA000000.toInt()) // 半透明黑色
        }

        // 创建显示截图的 ImageView
        val screenshotImageView = ImageView(activity).apply {
            setImageBitmap(fullScreenBitmap)
            scaleType = ImageView.ScaleType.FIT_XY
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // 添加 overlay 到根布局
        val container = activity.findViewById<FrameLayout>(android.R.id.content)
        if (selectionOverlayView.parent == null) {
            container.addView(screenshotImageView)   // 2. 截图图像
            container.addView(maskView)              // 1. 灰幕
            container.addView(selectionOverlayView)  // 3. 框选层
        }

        Toast.makeText(activity, "按住屏幕进行拖动框选区域", Toast.LENGTH_SHORT).show()

        val touchListener = View.OnTouchListener { _, event ->
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

                    if (startTouch == endTouch) {
                        Toast.makeText(activity, "已取消截图", Toast.LENGTH_SHORT).show()
                        cleanup(container, screenshotImageView)
                        onCaptureListener?.invoke(null)
                        return@OnTouchListener true
                    }

                    val selectedRect = selectionOverlayView.getSelectedRect()

                    val croppedBitmap = if (selectedRect != null && selectedRect.width() > 0 && selectedRect.height() > 0) {
                        Bitmap.createBitmap(
                            fullScreenBitmap,
                            selectedRect.left,
                            selectedRect.top,
                            selectedRect.width(),
                            selectedRect.height()
                        )
                    } else {
                        fullScreenBitmap
                    }

                    onCaptureListener?.invoke(croppedBitmap)
                    cleanup(container, screenshotImageView)
                }
            }
            true
        }

        selectionOverlayView.isFocusableInTouchMode = true
        selectionOverlayView.requestFocus()

        selectionOverlayView.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_SPACE && event.action == KeyEvent.ACTION_DOWN) {
                onCaptureListener?.invoke(fullScreenBitmap)
                cleanup(container, screenshotImageView)
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

    fun cleanup(container: FrameLayout, screenshotImageView: ImageView? = null) {
        selectionOverlayView.clearSelection()
        selectionOverlayView.setOnTouchListener(null)
        selectionOverlayView.setOnKeyListener(null)

        if (selectionOverlayView.parent != null) {
            container.removeView(selectionOverlayView)
        }

        // 移除灰幕
        maskView?.let {
            container.removeView(it)
            maskView = null
        }

        // 移除截图背景
        screenshotImageView?.let {
            if (it.parent != null) {
                container.removeView(it)
            }
        }

        isBoxSelectEnabled = false
        onCaptureListener = null
    }




}
