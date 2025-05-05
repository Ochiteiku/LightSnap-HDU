package com.electroboys.lightsnap.domain.screenshot

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.view.View

class SelectView(context: Context) : View(context) {
    private var startPoint = PointF(0f, 0f)
    private var endPoint = PointF(0f, 0f)

    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val maskPaint = Paint().apply {
        color = 0xAA000000.toInt() // 半透明黑色
        style = Paint.Style.FILL
    }

    fun setSelection(start: PointF, end: PointF) {
        startPoint.set(start)
        endPoint.set(end)
        invalidate()
    }

    fun getSelectedRect(): Rect? {
        if (startPoint == endPoint) return null
        return Rect(
            minOf(startPoint.x, endPoint.x).toInt(),
            minOf(startPoint.y, endPoint.y).toInt(),
            maxOf(startPoint.x, endPoint.x).toInt(),
            maxOf(startPoint.y, endPoint.y).toInt()
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val selectedRect = getSelectedRect()
        if (selectedRect != null) {
            val width = width.toFloat()
            val height = height.toFloat()

            // 绘制遮罩层（四个方向）
            canvas.drawRect(0f, 0f, width, selectedRect.top.toFloat(), maskPaint)
            canvas.drawRect(0f, selectedRect.top.toFloat(), selectedRect.left.toFloat(), selectedRect.bottom.toFloat(), maskPaint)
            canvas.drawRect(selectedRect.right.toFloat(), selectedRect.top.toFloat(), width, selectedRect.bottom.toFloat(), maskPaint)
            canvas.drawRect(0f, selectedRect.bottom.toFloat(), width, height, maskPaint)

            // 绘制选框边框
            canvas.drawRect(
                selectedRect.left.toFloat(),
                selectedRect.top.toFloat(),
                selectedRect.right.toFloat(),
                selectedRect.bottom.toFloat(),
                paint
            )
        }
    }

    fun clearSelection() {
        startPoint.set(0f, 0f)
        endPoint.set(0f, 0f)
        invalidate()
    }
}
