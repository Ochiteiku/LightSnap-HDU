package com.electroboys.lightsnap.ui.main.view.scrollshot

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class BlockerView(context: Context) : View(context) {
    private val paint = Paint().apply {
        color = Color.parseColor("#88000000") // 半透明黑色遮罩
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }
}
