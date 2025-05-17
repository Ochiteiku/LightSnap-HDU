package com.electroboys.lightsnap.ui.main.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*

import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.data.screenshot.BitmapCache
import com.electroboys.lightsnap.ui.main.view.scrollshot.ScrollShotPreviewView

class ScrollShotSelectionActivity : AppCompatActivity() {

    private lateinit var previewCropView: ScrollShotPreviewView
    lateinit var scrollView: ScrollView
    private lateinit var scrollBarThumb: View
    private var originalBitmap: Bitmap? = null

    companion object {
        const val EXTRA_BITMAP_KEY = "EXTRA_BITMAP_KEY"

        fun createIntent(context: Context, bitmapKey: String): Intent {
            return Intent(context, ScrollShotSelectionActivity::class.java).apply {
                putExtra(EXTRA_BITMAP_KEY, bitmapKey)
            }
        }

        fun getCroppedBitmapKey(intent: Intent): String? {
            return intent.getStringExtra(EXTRA_BITMAP_KEY)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrollshot_selection)

        // 初始化 bitmap
        val bitmapKey = intent.getStringExtra(EXTRA_BITMAP_KEY)
        originalBitmap = bitmapKey?.let { BitmapCache.getBitmap(it) }

        if (originalBitmap == null) {
            Log.e("ScrollShotSelectionActivity", "Bitmap is null, key: $bitmapKey")
            Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show()
            finish()
            return
        } else {
            Log.d("ScrollShotSelectionActivity", "Bitmap loaded, size: ${originalBitmap!!.width}x${originalBitmap!!.height}")
        }

        // 绑定视图
        previewCropView = findViewById(R.id.preview_crop_view)
        scrollView = findViewById(R.id.scrollView)
        scrollBarThumb = findViewById(R.id.scrollBarThumb)

        // 设置 bitmap 到 PreviewView
        previewCropView.setBitmap(originalBitmap)

        //  绑定按钮
        val btnCancel = findViewById<Button>(R.id.btn_cancel)
        val btnConfirm = findViewById<Button>(R.id.btn_confirm)

        btnCancel.setOnClickListener {
            finish()
        }

        btnConfirm.setOnClickListener {
            val cropped = previewCropView.getSelectedRegion()
            val croppedKey = cropped?.let { BitmapCache.cacheBitmap(it) }

            val result = Intent().apply {
                putExtra(ScrollShotSelectionActivity.EXTRA_BITMAP_KEY, croppedKey)
            }

            setResult(Activity.RESULT_OK, result)
            finish()
        }

        // 更新滑块初始状态
        updateScrollBar()

        // 监听滑块拖动
        setupScrollBarTouchListener()

        // 监听 ScrollView 滚动（同步滑块位置）
        setupScrollViewListener()
    }

    // 计算滑块大小 + 初始位置
    private fun updateScrollBar() {
        val totalImageHeight = previewCropView.height
        val visibleHeight = resources.displayMetrics.heightPixels

        if (totalImageHeight <= visibleHeight) {
            scrollBarThumb.visibility = View.GONE
            return
        }

        val parentHeight = scrollView.height
        val thumbHeight = (parentHeight * visibleHeight / totalImageHeight).toInt()
        val currentTop = (scrollView.scrollY.toFloat() / (totalImageHeight - visibleHeight)) * (parentHeight - thumbHeight)

        val params = scrollBarThumb.layoutParams
        params.height = thumbHeight
        scrollBarThumb.layoutParams = params
        scrollBarThumb.translationY = currentTop
    }

    // 拖动滑块时更新 ScrollView 的滚动位置
    @SuppressLint("ClickableViewAccessibility")
    private fun setupScrollBarTouchListener() {
        scrollBarThumb.setOnTouchListener { _, event ->
            val totalImageHeight = previewCropView.height
            val visibleHeight = resources.displayMetrics.heightPixels

            if (totalImageHeight <= visibleHeight) return@setOnTouchListener false

            val parentHeight = scrollView.height
            val scrollRange = totalImageHeight - visibleHeight
            val thumbHeight = scrollBarThumb.layoutParams.height

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 禁止选取线处理
                    previewCropView.setDraggingState(true)
                }
                MotionEvent.ACTION_MOVE -> {
                    val rawY = event.y
                    val maxRawY = (parentHeight - thumbHeight).toFloat()
                    val clampedY = rawY.coerceIn(0f, maxRawY)

                    // 更新滑块位置
                    scrollBarThumb.translationY = clampedY

                    // 计算滚动比例
                    val scrollRatio = clampedY / (parentHeight - thumbHeight)
                    val scrollY = (scrollRatio * scrollRange).toInt()

                    // 手动滚动 ScrollView
                    scrollView.scrollTo(0, scrollY)
                }
                MotionEvent.ACTION_UP -> {
                    previewCropView.setDraggingState(false)
                }
            }

            true
        }
    }

    // 当 ScrollView 滚动时，更新滑块位置
    private fun setupScrollViewListener() {
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val totalImageHeight = previewCropView.height
            val visibleHeight = resources.displayMetrics.heightPixels

            if (totalImageHeight > visibleHeight) {
                val parentHeight = scrollView.height
                val thumbHeight = scrollBarThumb.layoutParams.height
                val scrollY = scrollView.scrollY

                val ratio = scrollY.toFloat() / (totalImageHeight - visibleHeight)
                val maxTranslation = (parentHeight - thumbHeight).toFloat()
                scrollBarThumb.translationY = ratio * maxTranslation
            }
        }
    }

    // 暴露给 ScrollShotPreviewView 调用
    fun setDraggingLine(isDragging: Boolean) {
        scrollView.requestDisallowInterceptTouchEvent(isDragging)
    }

    // 更新滑块（供外部调用）
    fun updateScrollBarFromExternalScroll(scrollY: Int) {
        val totalImageHeight = previewCropView.height
        val visibleHeight = resources.displayMetrics.heightPixels

        if (totalImageHeight <= visibleHeight) return

        val parentHeight = scrollView.height
        val scrollRange = totalImageHeight - visibleHeight
        val thumbHeight = scrollBarThumb.layoutParams.height

        val scrollRatio = scrollY.toFloat() / scrollRange
        val maxTranslation = parentHeight - thumbHeight
        scrollBarThumb.translationY = scrollRatio * maxTranslation
    }
}
