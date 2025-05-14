package com.electroboys.lightsnap.ui.main.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.withMatrix
import com.electroboys.lightsnap.ui.main.view.GraffitiView.onBitmapChangeListener

class FrameSelectView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private lateinit var bitmap: Bitmap
    private var startX = 0f
    private var startY = 0f
    private var endX = 0f
    private var endY = 0f
    private val paint = Paint()
    private var shapeType = SHAPE_RECTANGLE // SHAPE_CIRCLE 或 SHAPE_RECTANGLE
    // 新增矩阵相关参数
    private val drawMatrix = Matrix()
    private val srcRect = RectF()
    private val dstRect = RectF()
    private val inverseMatrix = Matrix()
    private var isDrawing = false
    init {
        paint.isAntiAlias = true
        paint.strokeWidth = 5f // 默认线条宽度
        paint.color = Color.WHITE // 默认线条颜色
        paint.style = Paint.Style.STROKE // 设置为只画边框
        //抗锯齿
        paint.flags = Paint.ANTI_ALIAS_FLAG
    }

    fun setBitmap(externalBitmap: Bitmap) {
        bitmap = externalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        updateMatrix()
        invalidate() // 重绘view
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateMatrix()
    }

    private fun updateMatrix() {
        bitmap?.let { bmp ->
            // 计算原始边界
            srcRect.set(0f, 0f, bmp.width.toFloat(), bmp.height.toFloat())

            // 计算目标边界（保持比例居中）
            dstRect.set(0f, 0f, width.toFloat(), height.toFloat())
            drawMatrix.setRectToRect(srcRect, dstRect, Matrix.ScaleToFit.CENTER)

            // 准备反向矩阵（用于坐标转换）
            drawMatrix.invert(inverseMatrix)
        }
    }

    fun setShapeType(shapeType: Int) {
        this.shapeType = shapeType
        invalidate()
    }

    fun setLineColor(color: Int) {
        paint.color = color
        invalidate()
    }

    fun setLineWidth(style: Int) {
        when (style) {
            0 -> {
                paint.strokeWidth = 5f
            }
            1 -> {
                paint.strokeWidth = 10f
            }
            else -> {
                paint.strokeWidth = 15f
            }
        }

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let {
            canvas.withMatrix(drawMatrix) {
                drawBitmap(it, 0f, 0f, null)
            }
        }
        if (isDrawing) {
            drawShape(canvas)
        }
    }
    private fun mapRectToBitmap(startX: Float, startY: Float, endX: Float, endY: Float): RectF {
        val scaleX = bitmap.width / dstRect.width()
        val scaleY = bitmap.height / dstRect.height()
        val transX = -dstRect.left * scaleX
        val transY = -dstRect.top * scaleY
        return RectF(
            (startX * scaleX + transX).toFloat(),
            (startY * scaleY + transY).toFloat(),
            (endX * scaleX + transX).toFloat(),
            (endY * scaleY + transY).toFloat()
        )
    }

    private fun drawShape(canvas: Canvas) {
        when (shapeType) {
            SHAPE_RECTANGLE -> {
                canvas.drawRect(startX, startY, endX, endY, paint)
            }
            SHAPE_CIRCLE -> {
                paint.isAntiAlias = true // 确保抗锯齿开启
                val cx = (startX + endX) / 2
                val cy = (startY + endY) / 2
                val radius = Math.min(Math.abs(endX - startX), Math.abs(endY - startY)) / 2
                canvas.drawCircle(cx, cy, radius, paint)
            }
        }
    }
    private fun drawCircle(canvas: Canvas) {

        val centerX = (startX + endX) / 2
        val centerY = (startY + endY) / 2
        val radius = Math.abs(endX - startX) / 2
        canvas.drawCircle(centerX, centerY, radius, paint)
    }

    private fun drawRectangle(canvas: Canvas) {
        canvas.drawRect(startX, startY, endX, endY, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                endX = startX
                endY = startY
                isDrawing = true
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                endX = event.x
                endY = event.y
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                endX = event.x
                endY = event.y
                drawShapeOnBitmap()
                isDrawing = false
                invalidate()
                // 确保获取最新bitmap
                bitmap?.let {
                    bitmapChangeListener?.onBitmapChange(it.copy(Bitmap.Config.ARGB_8888, false))
                }
            }
        }
        return true
    }
    private var bitmapChangeListener: onBitmapChangeListener? = null

    fun setOnBitmapChangeListener(listener: onBitmapChangeListener) {
        bitmapChangeListener = listener
    }
    private fun drawShapeOnBitmap() {

        val canvas = Canvas(bitmap)
        canvas.concat(inverseMatrix)  // 同步视图变换
        when (shapeType) {
            SHAPE_RECTANGLE -> {
                canvas.drawRect(startX, startY, endX, endY, paint)
            }
            SHAPE_CIRCLE -> {
                val cx = (startX + endX) / 2
                val cy = (startY + endY) / 2
                val radius = Math.min(Math.abs(endX - startX), Math.abs(endY - startY)) / 2
                canvas.drawCircle(cx, cy, radius, paint)
            }
        }
    }
    companion object {
        const val SHAPE_CIRCLE = 1
        const val SHAPE_RECTANGLE = 0
    }
}
