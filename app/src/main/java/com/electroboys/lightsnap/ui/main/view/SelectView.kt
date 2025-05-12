package com.electroboys.lightsnap.ui.main.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class SelectView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    init {
        isClickable = false
        isFocusable = false
        isFocusableInTouchMode = false
    }

//    init {
//        setBackgroundColor(Color.argb(128, 255, 0, 0))  // 半透明红色背景，确认视图位置
//    }

    private var startPoint = PointF(0f, 0f)
    private var endPoint = PointF(0f, 0f)

    private val paint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        pathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
    }

    private val maskPaint = Paint().apply {
        color = 0xAA000000.toInt() // 半透明黑色
        style = Paint.Style.FILL
    }

    fun setSelection(start: PointF, end: PointF) {
//        Log.d("SelectView", "【调试】传入 setSelection 的起点: $start，终点: $end")
        startPoint.set(start.x, start.y)
        endPoint.set(end.x, end.y)
//        Log.d("SelectView", "更新后的 startPoint: $startPoint，endPoint: $endPoint")
        invalidate()
    }


    fun getSelectedRect(): Rect? {
        val left = minOf(startPoint.x, endPoint.x).toInt()
        val top = minOf(startPoint.y, endPoint.y).toInt()
        val right = maxOf(startPoint.x, endPoint.x).toInt()
        val bottom = maxOf(startPoint.y, endPoint.y).toInt()

        val rect = Rect(left, top, right, bottom)

        Log.d("SelectView", "有效选区: $rect")
        return rect
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        Log.d("SelectView", "onDraw called")
        val selectedRect = getSelectedRect()
        if (selectedRect != null && selectedRect.width() > 0 && selectedRect.height() > 0) {
            val width = width.toFloat()
            val height = height.toFloat()

            canvas.drawRect(0f, 0f, width, selectedRect.top.toFloat(), maskPaint)
            canvas.drawRect(0f, selectedRect.top.toFloat(), selectedRect.left.toFloat(), selectedRect.bottom.toFloat(), maskPaint)
            canvas.drawRect(selectedRect.right.toFloat(), selectedRect.top.toFloat(), width, selectedRect.bottom.toFloat(), maskPaint)
            canvas.drawRect(0f, selectedRect.bottom.toFloat(), width, height, maskPaint)

            canvas.drawRect(
                selectedRect.left.toFloat(),
                selectedRect.top.toFloat(),
                selectedRect.right.toFloat(),
                selectedRect.bottom.toFloat(),
                paint
            )
        }
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d("SelectView", "onSizeChanged w=$w h=$h")
    }

    fun clearSelection() {
        startPoint.set(0f, 0f)
        endPoint.set(0f, 0f)
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 明确返回 false，表示不消费事件，允许事件继续传递给父容器
        return false
    }
}