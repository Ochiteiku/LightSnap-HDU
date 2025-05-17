package com.electroboys.lightsnap.ui.main.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.withTranslation
import com.electroboys.lightsnap.domain.screenshot.watermark.WatermarkConfig

class WatermarkOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint()
    private var config = WatermarkConfig.Companion.default()
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

    // 将当前水印绘制到指定 Bitmap 上
    fun applyWatermarkToBitmap(original: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawBitmap(original, 0f, 0f, null)

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

        val scaleX = original.width.toFloat() / this.width.toFloat()
        val scaleY = original.height.toFloat() / this.height.toFloat()

        val spacingX = (config.horizontalSpacing * scaleX).toInt()
        val spacingY = (config.verticalSpacing * scaleY).toInt()

        for (x in 0..original.width step spacingX) {
            for (y in 0..original.height step spacingY) {
                canvas.withTranslation(x.toFloat(), y.toFloat()) {
                    rotate(config.rotation, centerX, centerY)
                    drawText(config.text, centerX, centerY, paint)
                }
            }
        }

        return result
    }
}
