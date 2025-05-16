package com.electroboys.lightsnap.domain.screenshot.modehandler

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import com.electroboys.lightsnap.data.screenshot.BitmapCache
import com.electroboys.lightsnap.data.screenshot.ImageHistory
import com.electroboys.lightsnap.domain.screenshot.ControlModeHandler
import com.electroboys.lightsnap.domain.screenshot.repository.ImageCropRepository
import com.electroboys.lightsnap.ui.main.activity.ScreenshotActivity.Companion.EXTRA_SCREENSHOT_KEY
import com.electroboys.lightsnap.ui.main.view.FrameSelectView
import com.electroboys.lightsnap.ui.main.view.GraffitiView
import com.electroboys.lightsnap.ui.main.view.SelectView
import com.electroboys.lightsnap.ui.main.viewmodel.ScreenshotViewModel

class CropModeHandler(
    private val context: Context,
    private val imageView: ImageView,
    private val imageContainer: View,
    private val selectView: SelectView,
    private val graffitiView: GraffitiView,
    private val cropRepository: ImageCropRepository,
    private val viewModel: ScreenshotViewModel,
    private val selectionHintView: View
) : ControlModeHandler {

    private var isTouchListenerAttached = false
    private var isActive = false

    override fun apply(
        context: Context,
        bitmap: Bitmap,
        graffitiView: GraffitiView,
        frameSelectView: FrameSelectView,
        exControlFrame: ViewGroup
    ) {
        if (!isActive) {
            viewModel.clearSelectionRect() // 🔧 清除旧的选区
            setupObservers()
            setupTouchListener()
            selectionHintView.visibility = View.VISIBLE
            isActive = true
        }
    }

    fun exit() {
        selectView.clearSelection()
        selectionHintView.visibility = View.GONE
        isActive = false
        isTouchListenerAttached = false // ✅ 关键一步
        imageContainer.setOnTouchListener(null)
    }

    private fun setupObservers() {
        viewModel.selectionRect.observe(context as LifecycleOwner) { rect ->
            if (rect != null) {
                selectView.setSelection(
                    PointF(rect.left.toFloat(), rect.top.toFloat()),
                    PointF(rect.right.toFloat(), rect.bottom.toFloat())
                )
                selectionHintView.visibility = if (rect.isEmpty) View.VISIBLE else View.GONE
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        if (isTouchListenerAttached) return
        isTouchListenerAttached = true

        imageContainer.setOnTouchListener { _, event ->
            if (viewModel.isSelectionEnabled.value != true) return@setOnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> viewModel.startDrag(event.x, event.y)
                MotionEvent.ACTION_MOVE -> if (viewModel.isDragging.value == true) viewModel.updateDrag(event.x, event.y)
                MotionEvent.ACTION_UP -> {
                    viewModel.endDrag(event.x, event.y)

                    val drawable = imageView.drawable as? BitmapDrawable
                    val rect = viewModel.selectionRect.value
                    if (drawable != null && rect != null && !rect.isEmpty) {
                        val cropped = cropRepository.cropBitmap(drawable.bitmap, rect, imageView.imageMatrix)
                        if (cropped != null) {
                            imageView.setImageBitmap(cropped)
                            val key = BitmapCache.cacheBitmap(cropped)
                            (context as Activity).intent.putExtra(EXTRA_SCREENSHOT_KEY, key)
                            ImageHistory.push(key)
                            selectView.clearSelection()
                            selectionHintView.visibility = View.VISIBLE
                            graffitiView.setBitmap(cropped)
                            Toast.makeText(context, "已裁剪并更新图像", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "裁剪失败或区域无效", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "无效选区或图片为空", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            true
        }
    }
}