package com.electroboys.lightsnap.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.graphics.withTranslation
import com.electroboys.lightsnap.domain.screenshot.watermark.WatermarkConfig

object WatermarkUtil {

    fun addWatermark(
        originalBitmap: Bitmap,
        config: WatermarkConfig
    ): Bitmap {
        val resultBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(resultBitmap)

        val paint = Paint().apply {
            color = config.color
            this.alpha = config.alpha
            textSize = config.textSize
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val textBounds = Rect()
        paint.getTextBounds(config.text, 0, config.text.length, textBounds)

        val textWidth = textBounds.width()
        val textHeight = textBounds.height()

        val centerX = textWidth / 2f
        val centerY = textHeight / 2f

        for (x in 0..canvas.width step config.horizontalSpacing) {
            for (y in 0..canvas.height step config.verticalSpacing) {
                canvas.withTranslation(x.toFloat(), y.toFloat()) {
                    rotate(config.rotation, centerX, centerY)
                    drawText(config.text, centerX, centerY, paint)
                }
            }
        }

        return resultBitmap
    }
}
