package com.electroboys.lightsnap.ui.main.view.scrollshot

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.graphics.withTranslation
import com.electroboys.lightsnap.ui.main.activity.ScrollShotSelectionActivity
import kotlin.math.abs
import kotlin.math.min
import androidx.core.graphics.toColorInt

class ScrollShotPreviewView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    // Bitmap 显示相关
    private var bitmap: Bitmap? = null
    private var scale = 1f
    private var offsetX = 0f
    private var offsetY = 0f

    // 选取线状态
    private var startY = 0f
    private var endY = 0f
    private var selectedLine: SelectedLine? = null
    private var imageYForGuide = 0f
    private var isDraggingLine = false

    // 点击容忍范围（用于判断是否点击在线上）
    private val CLICK_TOLERANCE_DP = 30f
    private val CLICK_TOLERANCE_PX: Float by lazy {
        resources.displayMetrics.density * CLICK_TOLERANCE_DP
    }

    // 绘制用画笔
    private val linePaint = Paint().apply {
        color = Color.RED
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }

    private val guideLinePaint = Paint().apply {
        color = "#8800AAFF".toColorInt()
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val hintPaint = Paint().apply {
        textSize = 48f
        color = "#666666".toColorInt()
        textAlign = Paint.Align.CENTER
        alpha = 180
    }

    private var screenHeight = 0f

    enum class SelectedLine { START, END }
    enum class SelectionState { None, SelectingTop, SelectingBottom }

    private var selectionState = SelectionState.None
    private var activity: ScrollShotSelectionActivity? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        recomputeScaleAndOffset()
    }

    private val selectionRectPaint = Paint().apply {
        color = "#44FF0000".toColorInt() // 半透明红色
        style = Paint.Style.FILL
    }

    private fun recomputeScaleAndOffset() {
        val b = bitmap ?: return

        val viewWidth = width
        val viewHeight = height

        screenHeight = height.toFloat()

        scale = min(
            viewWidth / b.width.toFloat(),
            viewHeight / b.height.toFloat()
        )

        offsetX = (viewWidth - b.width * scale) / 2f
        offsetY = (viewHeight - b.height * scale) / 2f

        if (startY == 0f && endY == 0f) {
            startY = 0f
            endY = b.height * scale
        }
    }

    fun setBitmap(bitmap: Bitmap?) {
        this.bitmap = bitmap
        recomputeScaleAndOffset()
        postInvalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val b = bitmap ?: return

        canvas.withTranslation(offsetX, offsetY) {
            canvas.scale(scale, scale)

            canvas.drawBitmap(b, 0f, 0f, Paint())

            // 绘制选区框（调试用）
            canvas.drawRect(0f, startY, b.width.toFloat(), endY, selectionRectPaint)

            // 绘制定位线
            canvas.drawLine(0f, startY, b.width.toFloat(), startY, linePaint)
            canvas.drawLine(0f, endY, b.width.toFloat(), endY, linePaint)

            // 绘制引导线（拖动时显示）
            selectedLine?.let {
                canvas.drawLine(0f, imageYForGuide, b.width.toFloat(), imageYForGuide, guideLinePaint)
            }
        }

        // 提示信息绘制
        val hintText = when (selectionState) {
            SelectionState.None -> ""
            SelectionState.SelectingTop -> "请点击选择起始线"
            SelectionState.SelectingBottom -> "请拖动选择结束线"
        }

        if (hintText.isNotEmpty()) {
            canvas.drawText(hintText, width / 2f, height / 2f, hintPaint)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        activity = context as? ScrollShotSelectionActivity
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val b = bitmap
        val rawY = event.y
        val imageY = (rawY - offsetY) / scale
        imageYForGuide = imageY

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                activity?.setDraggingLine(true)
                parent.requestDisallowInterceptTouchEvent(true)

                when (selectionState) {
                    SelectionState.None -> {
                        selectionState = SelectionState.SelectingTop
                        Toast.makeText(context, "请点击选择起始线", Toast.LENGTH_SHORT).show()
                    }

                    SelectionState.SelectingTop -> {
                        val startDiff = abs(startY - imageY)
                        if (startDiff < CLICK_TOLERANCE_PX) {
                            selectedLine = SelectedLine.START
                            isDraggingLine = true
                            Toast.makeText(context, "请选择结束线", Toast.LENGTH_SHORT).show()
                        } else {
                            startY = imageY.coerceIn(0f..endY - 50f)
                            selectionState = SelectionState.SelectingBottom
                            Toast.makeText(context, "请选择结束线", Toast.LENGTH_SHORT).show()
                        }
                    }

                    SelectionState.SelectingBottom -> {
                        val endDiff = abs(endY - imageY)
                        if (endDiff < CLICK_TOLERANCE_PX) {
                            selectedLine = SelectedLine.END
                            isDraggingLine = true
                        } else {
                            selectedLine = null
                        }
                    }
                }

                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDraggingLine) {
                    parent.requestDisallowInterceptTouchEvent(true)

                    selectedLine?.let { line ->
                        when (line) {
                            SelectedLine.START -> {
                                val newStart = imageY.coerceIn(0f..endY - 50f)
                                startY = newStart
                            }
                            SelectedLine.END -> {
                                val maxEnd = (b?.height?.times(scale))?.coerceAtLeast(startY + 50f)
                                val newEnd = imageY.coerceIn(startY + 50f..  maxEnd!!)
                                endY = newEnd
                            }
                        }
                    }

                    invalidate()
                }
            }

            MotionEvent.ACTION_UP -> {
                isDraggingLine = false
                selectedLine = null
                activity?.setDraggingLine(false)
                parent.requestDisallowInterceptTouchEvent(false)
                invalidate()
            }
        }

        return true
    }

    // 供外部调用更新滚动位置（比如 ScrollView 滚动时）
    fun updateScrollBarPosition(scrollY: Int) {
        activity?.updateScrollBarFromExternalScroll(scrollY)
    }

    fun getSelectedRegion(): Bitmap? {
        val b = bitmap ?: return null
        Log.d("SelectionDebug", "Bitmap size: ${b.width} x ${b.height}")

        val selectedStart = startY.toInt()
        val selectedEnd = endY.toInt()

        val imageStart = (selectedStart / scale).toInt()
        val imageEnd = (selectedEnd / scale).toInt()

        val clampedImageStart = imageStart.coerceIn(0, b.height)
        val clampedImageEnd = imageEnd.coerceIn(0, b.height)

        val height = clampedImageEnd - clampedImageStart

        if (height <= 0) {
            Toast.makeText(context, "选区太小，请重新选择", Toast.LENGTH_SHORT).show()
            return null
        }

        try {
            return Bitmap.createBitmap(b, 0, clampedImageStart, b.width, height)
        } catch (e: Exception) {
            Log.e("ScrollShotPreviewView", "裁剪失败", e)
            Toast.makeText(context, "裁剪失败，请调整后再试", Toast.LENGTH_SHORT).show()
            return null
        }
    }


    fun setDraggingState(isDragging: Boolean) {
        this.isDraggingLine = isDragging
    }
}
