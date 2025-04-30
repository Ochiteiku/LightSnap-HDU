package com.electroboys.lightsnap.domain.screenshot


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.view.View

class SelectView (context: Context) : View(context){
    private var startPoint = PointF(0f, 0f)
    private var endPoint = PointF(0f, 0f)
    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    fun setSelection(start: PointF, end: PointF) {
        startPoint.set(start)
        endPoint.set(end)
        invalidate()
    }

    // 获取用户选择的矩形区域
    fun getSelectedRect(): Rect? {
        if (startPoint == endPoint) return null
        return Rect(
            minOf(startPoint.x, endPoint.x).toInt(),
            minOf(startPoint.y, endPoint.y).toInt(),
            maxOf(startPoint.x, endPoint.x).toInt(),
            maxOf(startPoint.y, endPoint.y).toInt()
        )
    }

    fun clearSelection() {
        startPoint.set(0f, 0f)
        endPoint.set(0f, 0f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (startPoint != endPoint) {
            canvas.drawRect(
                minOf(startPoint.x, endPoint.x),
                minOf(startPoint.y, endPoint.y),
                maxOf(startPoint.x, endPoint.x),
                maxOf(startPoint.y, endPoint.y),
                paint
            )
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        // 可在这里触发额外逻辑，例如回调给 Activity
        return true
    }
}