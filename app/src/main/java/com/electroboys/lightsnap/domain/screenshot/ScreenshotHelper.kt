package com.electroboys.lightsnap.domain.screenshot

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.ui.main.activity.BaseActivity.BaseActivity
import com.electroboys.lightsnap.ui.main.adapter.LibraryPictureAdapter
import com.electroboys.lightsnap.ui.main.view.SelectView
import com.electroboys.lightsnap.utils.ScreenshotUtil

class ScreenshotHelper(private val activity: AppCompatActivity) {

    private val selectionOverlayView = SelectView(activity)
    private val startTouch = PointF()
    private val endTouch = PointF()
    private var isBoxSelectEnabled = false
    private var onCaptureListener: ((Bitmap?) -> Unit)? = null
    private var maskView: View? = null// 灰幕
    private var backgroundImageView: ImageView? = null
    private var adapter:LibraryPictureAdapter ?= null

    @SuppressLint("ClickableViewAccessibility", "NotifyDataSetChanged")
    fun enableBoxSelectOnce(onCapture: (Bitmap?) -> Unit) {
        this.onCaptureListener = onCapture
        if (isBoxSelectEnabled) return

        isBoxSelectEnabled = true
        selectionOverlayView.clearSelection()

        val container = activity.findViewById<FrameLayout>(android.R.id.content)

        val recyclerView = activity.findViewById<RecyclerView>(R.id.library_picture)
        if (recyclerView != null) {
            adapter = recyclerView.adapter as? LibraryPictureAdapter
            LibraryPictureAdapter.isScreenshotMode = true
            adapter?.notifyDataSetChanged()
        }

        // 强制等待 UI 更新完成
        Handler(Looper.getMainLooper()).postDelayed({
        // 执行异步截图（支持 SurfaceView）
        ScreenshotUtil.captureWithStatusBar(activity) { fullScreenshot ->
            LibraryPictureAdapter.isScreenshotMode = false
            adapter?.notifyDataSetChanged()
            // 截图完成后的逻辑，确保在主线程执行
            activity.runOnUiThread {
                val imageView = ImageView(activity).apply {
                    setImageBitmap(fullScreenshot)
                    scaleType = ImageView.ScaleType.FIT_XY
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                backgroundImageView = imageView

                // 创建灰幕 View
                maskView = View(activity).apply {
                    setBackgroundColor(0xAA000000.toInt()) // 半透明黑色
                }

                if (selectionOverlayView.parent == null) {
                    container.addView(imageView)            // 背景图层
                    container.addView(maskView)             // 灰幕
                    container.addView(selectionOverlayView) // 框选层
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
                                cleanup(container)
                                onCaptureListener?.invoke(null)

                                // 主动重置 BaseActivity 的截图状态
                                activity.runOnUiThread {
                                    val baseActivity = activity as? BaseActivity
                                    baseActivity?.isTakingScreenshot = false
                                    baseActivity?.currentScreenshotHelper = null
                                }

                                return@OnTouchListener true
                            }

                            val selectedRect = selectionOverlayView.getSelectedRect()
                            val croppedBitmap = if (selectedRect != null &&
                                selectedRect.left >= 0 && selectedRect.top >= 0 &&
                                selectedRect.right <= fullScreenshot.width &&
                                selectedRect.bottom <= fullScreenshot.height) {
                                Bitmap.createBitmap(
                                    fullScreenshot,
                                    selectedRect.left, selectedRect.top,
                                    selectedRect.width(), selectedRect.height()
                                )
                            } else {
                                fullScreenshot
                            }

                            onCaptureListener?.invoke(croppedBitmap)
                            cleanup(container)
                        }
                    }
                    true
                }

                selectionOverlayView.isFocusableInTouchMode = true
                selectionOverlayView.requestFocus()

                selectionOverlayView.setOnKeyListener { _, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_SPACE && event.action == KeyEvent.ACTION_DOWN) {
                        onCaptureListener?.invoke(fullScreenshot)
                        cleanup(container)
                        true
                    } else {
                        false
                    }
                }

                selectionOverlayView.setOnTouchListener(touchListener)
            }
        }
        }, 200) // 小小延迟确保界面刷新完成
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

        maskView?.let {
            container.removeView(it)
            maskView = null
        }

        backgroundImageView?.let {
            if (it.parent != null) {
                container.removeView(it)
            }
            backgroundImageView = null
        }

        isBoxSelectEnabled = false
        onCaptureListener = null
    }


}
