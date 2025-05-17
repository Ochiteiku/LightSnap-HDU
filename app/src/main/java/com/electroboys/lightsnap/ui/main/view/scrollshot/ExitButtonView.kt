package com.electroboys.lightsnap.ui.main.view.scrollshot

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import androidx.core.graphics.toColorInt

class ExitButtonView(context: Context) : View(context) {
    private val backgroundPaint = Paint().apply {
        color = "#88000000".toColorInt() // 半透明黑色背景
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 36f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        isAntiAlias = true
    }

    private val buttonText = "退出截图"

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 绘制矩形背景
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRect(rect, backgroundPaint)

        // 绘制文字
        val textHeight = -textPaint.fontMetrics.ascent // 计算垂直居中偏移量
        canvas.drawText(buttonText, width / 2f, height / 2f + textHeight / 2, textPaint)
    }
}
