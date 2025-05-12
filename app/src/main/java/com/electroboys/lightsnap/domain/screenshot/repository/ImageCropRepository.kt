package com.electroboys.lightsnap.domain.screenshot.repository

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect

class ImageCropRepository {
    fun cropBitmap(
        bitmap: Bitmap,
        rect: Rect,
        imageMatrix: Matrix
    ): Bitmap? {
        val values = FloatArray(9)
        imageMatrix.getValues(values)

        val scaleX = values[Matrix.MSCALE_X]
        val scaleY = values[Matrix.MSCALE_Y]
        val transX = values[Matrix.MTRANS_X]
        val transY = values[Matrix.MTRANS_Y]

        val left = ((rect.left - transX) / scaleX).toInt()
        val top = ((rect.top - transY) / scaleY).toInt()
        val right = ((rect.right - transX) / scaleX).toInt()
        val bottom = ((rect.bottom - transY) / scaleY).toInt()

        val cropRect = Rect(
            maxOf(0, minOf(left, right)),
            maxOf(0, minOf(top, bottom)),
            minOf(bitmap.width, maxOf(left, right)),
            minOf(bitmap.height, maxOf(top, bottom))
        )

        return if (cropRect.width() > 0 && cropRect.height() > 0) {
            Bitmap.createBitmap(bitmap, cropRect.left, cropRect.top, cropRect.width(), cropRect.height())
        } else null
    }
}