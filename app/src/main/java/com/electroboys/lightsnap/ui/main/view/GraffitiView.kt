package com.electroboys.lightsnap.ui.main.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.withMatrix
import kotlin.math.abs


class GraffitiView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val path = Path()
    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null
    private var isMosaicMode = false
    private var maskBlur: Float = 50f // 默认模糊度为50
    private var mosaicRadius: Int = 2 // 默认马赛克半径为20

    init {
    }

    // 设置bitmap时需要更新矩阵
    fun setBitmap(externalBitmap: Bitmap) {
        bitmap = externalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        canvas = Canvas(bitmap!!)
        updateMatrix()
        invalidate()
    }

    // 设置是否为马赛克模式
    fun setMosaicMode(mosaicMode: Boolean) {
        isMosaicMode = mosaicMode
        if (!isMosaicMode){
            initPaint()
        }
    }
    private fun initPaint() {
        paint = Paint().apply {
            isAntiAlias = true
            isDither = true
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 6f
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
    }

    // 获取是否为马赛克模式
    fun isMosaicMode(): Boolean {
        return isMosaicMode
    }

    // 清除画布
    fun clearCanvas() {
        bitmap?.eraseColor(Color.WHITE)
        invalidate()
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

    // 新增矩阵相关参数
    private val drawMatrix = Matrix()
    private val srcRect = RectF()
    private val dstRect = RectF()
    private val inverseMatrix = Matrix()
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let {
            canvas.withMatrix(drawMatrix) {
                drawBitmap(it, 0f, 0f, null)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 转换触摸坐标到bitmap坐标系
        val adjustedEvent = MotionEvent.obtain(event)
        adjustedEvent.transform(inverseMatrix)

        val x = adjustedEvent.x.coerceIn(0f, bitmap?.width?.toFloat() ?: 0f).toInt()
        val y = adjustedEvent.y.coerceIn(0f, bitmap?.height?.toFloat() ?: 0f).toInt()


        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x.toFloat(), y.toFloat())
                canvas?.save()
            }

            MotionEvent.ACTION_MOVE -> {
                path.lineTo(x.toFloat(), y.toFloat())
                if (isMosaicMode) {
                    drawMosaic(x, y)
                } else {
                    drawPathOnBitmap()
                }
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                canvas?.restore()
                if (!isMosaicMode) {
                    drawPathOnBitmap() // 仅在非马赛克模式下确保路径被绘制到Bitmap上
                }
                path.reset()
                // 确保获取最新bitmap
                bitmap?.let {
                    bitmapChangeListener?.onBitmapChange(it.copy(Bitmap.Config.ARGB_8888, false))
                }
            }
        }
        return true
    }

    private fun drawPathOnBitmap() {
        paint.xfermode = null
        canvas?.drawPath(path, paint)
    }

    private fun drawMosaic(x: Int, y: Int) {
        // 定义三层参数（在类中声明为成员变量更佳）
        val blockSize = mosaicRadius * 2  // 实际绘制块尺寸
        val sampleRadius = mosaicRadius * 3 // 采样半径
        val blurRadius = maskBlur / 2      // 模糊半径与参数关联

        // 动态计算采样区域
        val startX = maxOf(0, x - sampleRadius)
        val endX = minOf(bitmap?.width ?: 0, x + sampleRadius)
        val startY = maxOf(0, y - sampleRadius)
        val endY = minOf(bitmap?.height ?: 0, y + sampleRadius)

        // 分步绘制马赛克
        for (i in startX until endX step blockSize) {
            for (j in startY until endY step blockSize) {
                // 获取当前区块平均色（扩大采样范围）
                val avgColor = getAverageColorAroundPoint(
                    i + blockSize / 2,
                    j + blockSize / 2,
                    sampleRadius
                )

                // 配置绘制参数
                paint.color = avgColor
                paint.maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)

                // 绘制无缝衔接的色块
                val drawEndX = minOf(i + blockSize, endX)
                val drawEndY = minOf(j + blockSize, endY)
                canvas?.drawRect(
                    i.toFloat(),
                    j.toFloat(),
                    drawEndX.toFloat(),
                    drawEndY.toFloat(),
                    paint
                )
            }
        }
    }


    private fun getAverageColorAroundPoint(x: Int, y: Int, radius: Int): Int {
        var red = 0
        var green = 0
        var blue = 0
        var count = 0

        val startX = maxOf(0, x - radius)
        val endX = minOf(bitmap?.width ?: 0, x + radius)
        val startY = maxOf(0, y - radius)
        val endY = minOf(bitmap?.height ?: 0, y + radius)


        for (i in startX until endX) {
            for (j in startY until endY) {
                val pixelColor = bitmap?.getPixel(i, j) ?: Color.WHITE
                red += Color.red(pixelColor)
                green += Color.green(pixelColor)
                blue += Color.blue(pixelColor)
                count++
            }
        }

        return if (count == 0) Color.WHITE else Color.rgb(red / count, green / count, blue / count)
    }

    //设置笔刷大小 三种挡位
    fun setStrokeWidth(style: Int) {
        when (style) {
            0 -> {
                paint.strokeWidth = 6f
            }

            1 -> {
                paint.strokeWidth = 14f
            }

            2 -> {
                paint.strokeWidth = 24f
            }
        }
        invalidate() // 刷新视图以应用新的笔刷大小
    }

    //设置笔刷颜色
    fun setStrokeColor(color: Int) {
        paint.color = color

        invalidate()     //刷新视图
    }

    //设置马赛克半径 三种挡位
    fun setMosaicRadius(radius: Int) {
        Log.d("GraffitiView", "setMosaicRadius: $radius")
        when (radius) {
            0 -> mosaicRadius = 2
            1 -> mosaicRadius = 5
            2 -> mosaicRadius = 8
            else -> {
                mosaicRadius = 2 // 默认值为20
                Log.w("GraffitiView", "Invalid radius, defaulting to 20")
            }
        }
        invalidate() // 刷新视图以应用新的马赛克半径
    }

    //设置马赛克模糊度 0-100
    fun setMosaicBlur(blur: Int) {
        Log.d("GraffitiView", "setMosaicBlur: $blur")
        maskBlur = abs(blur - 100f);
        maskBlur = maskBlur.coerceIn(1f, 50f)

        Log.d("GraffitiView", "maskBlur: $maskBlur")

        invalidate() // 刷新视图以应用新的模糊度
    }

    interface onBitmapChangeListener {
        fun onBitmapChange(bitmap: Bitmap)
    }

    private var bitmapChangeListener: onBitmapChangeListener? = null

    fun setOnBitmapChangeListener(listener: onBitmapChangeListener) {
        bitmapChangeListener = listener
    }

    fun saveBitmap(): Bitmap? {
        return bitmap
    }

}
