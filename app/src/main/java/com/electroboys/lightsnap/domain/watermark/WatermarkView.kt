package com.electroboys.lightsnap.domain.watermark

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.withTranslation

class WatermarkOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint()
    private var config = WatermarkConfig.default()
    private val textBounds = Rect()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (config.text.isEmpty()) return

        paint.apply {
            color = config.color
            alpha = config.alpha
            textSize = config.textSize
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        paint.getTextBounds(config.text, 0, config.text.length, textBounds)
        val textWidth = textBounds.width()
        val textHeight = textBounds.height()

        val centerX = textWidth / 2f
        val centerY = textHeight / 2f
        val spacingX = config.horizontalSpacing
        val spacingY = config.verticalSpacing

        for (x in 0..width step spacingX) {
            for (y in 0..height step spacingY) {
                canvas.withTranslation(x.toFloat(), y.toFloat()) {
                    rotate(config.rotation, centerX, centerY)
                    drawText(config.text, centerX, centerY, paint)
                }
            }
        }
    }

    // 接收 WatermarkConfig 设置
    fun setWatermark(config: WatermarkConfig) {
        this.config = config
        invalidate()
    }
}
