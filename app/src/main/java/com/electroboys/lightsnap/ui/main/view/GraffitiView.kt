package com.electroboys.lightsnap.ui.main.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.withMatrix
import kotlin.math.*


class GraffitiView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 绘制模式枚举
    enum class DrawMode {
        GRAFFITI,   // 涂鸦模式
        ARROW,      // 箭头模式
        MOSAIC      // 马赛克模式
    }

    // 箭头数据类
    data class Arrow(
        val startX: Float,
        val startY: Float,
        val endX: Float,
        val endY: Float,
        var color: Int,
        val width: Float,
        val style: Int
    )

    // 当前绘制模式
    private var currentMode = DrawMode.GRAFFITI

    // 共享 Bitmap 和 Canvas
    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null

    private var style: Int = 1
    private var graffitiPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private val currentPath = Path()

    // 箭头相关属性
    private val arrowPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
        color = Color.RED
        strokeWidth = 5f
    }
    private val arrows = mutableListOf<Arrow>()
    private var currentArrow: Arrow? = null
    private val arrowHeadSize = 30f
    private var arrowStyle = 0 // 0: 实线, 1: 虚线

    private var isMosaicMode = false
    private var maskBlur: Float = 50f // 默认模糊度为50
    private var mosaicRadius: Int = 5 // 默认马赛克半径为20
    private var blurStyle = BlurMaskFilter.Blur.NORMAL // 0为实心，1为描边，2为虚线

    // 矩阵变换相关
    private val drawMatrix = Matrix()
    private val srcRect = RectF()
    private val dstRect = RectF()
    private val inverseMatrix = Matrix()

    // 初始化
    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null) // 马赛克需要软件渲染
    }

    // 设置绘制模式
    fun setDrawMode(mode: DrawMode) {
        currentMode = mode
        currentPath.reset()
        currentArrow = null
        invalidate()
    }

    // 设置Bitmap
    fun setBitmap(externalBitmap: Bitmap) {
        bitmap = externalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        canvas = Canvas(bitmap!!)
        updateMatrix()
        invalidate()
    }

    // 设置是否为马赛克模式
    fun setMosaicMode(mosaicMode: Boolean) {
        isMosaicMode = mosaicMode
        if (!isMosaicMode) {
            initPaint()
        }
    }

    // 绘制箭头
    private fun drawArrow(canvas: Canvas, arrow: Arrow) {
        val points = floatArrayOf(arrow.startX, arrow.startY, arrow.endX, arrow.endY)
        drawMatrix.mapPoints(points) // 坐标变换适配视图

        arrowPaint.color = arrow.color
        arrowPaint.strokeWidth = arrow.width

        // 设置箭头样式
        arrowPaint.pathEffect = if (arrow.style == 1) {
            DashPathEffect(floatArrayOf(20f, 10f), 0f)
        } else {
            null
        }

        // 绘制箭身
        canvas.drawLine(arrow.startX, arrow.startY, arrow.endX, arrow.endY, arrowPaint)

        // 绘制箭头头部
        val angle = atan2(arrow.endY - arrow.startY, arrow.endX - arrow.startX)
        val path = Path().apply {
            moveTo(arrow.endX, arrow.endY)
            lineTo(
                arrow.endX - arrowHeadSize * cos(angle - Math.PI / 6).toFloat(),
                arrow.endY - arrowHeadSize * sin(angle - Math.PI / 6).toFloat()
            )
            moveTo(arrow.endX, arrow.endY)
            lineTo(
                arrow.endX - arrowHeadSize * cos(angle + Math.PI / 6).toFloat(),
                arrow.endY - arrowHeadSize * sin(angle + Math.PI / 6).toFloat()
            )
        }
        canvas.drawPath(path, arrowPaint)
    }

    // 处理箭头触摸事件
    private fun handleArrowTouch(event: MotionEvent, x: Float, y: Float) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentArrow = Arrow(
                    x, y, x, y,
                    arrowPaint.color,
                    arrowPaint.strokeWidth,
                    arrowStyle
                )
            }

            MotionEvent.ACTION_MOVE -> {
                currentArrow = currentArrow?.copy(endX = x, endY = y)
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                currentArrow?.let {
                    // 将箭头绘制到Bitmap上
                    drawArrow(canvas!!, it)
                    arrows.add(it)
                    currentArrow = null
                    notifyBitmapChanged()
                }
            }
        }
    }

    // 箭头相关设置方法
    fun setArrowColor(color: Int) {
        arrowPaint.color = color
        invalidate()
    }

    fun setArrowWidth(width: Float) {
        arrowPaint.strokeWidth = width
        invalidate()
    }

    fun setArrowStyle(style: Int) {
        arrowStyle = style
        invalidate()
    }

    fun removeLastArrow() {
        if (arrows.isNotEmpty()) {
            arrows.removeAt(arrows.size - 1)
            redrawAll()
        }
    }

    private fun initPaint() {
        graffitiPaint = Paint().apply {
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


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let { bmp ->
            canvas.withMatrix(drawMatrix) {
                // 1. 绘制底层Bitmap
                drawBitmap(bmp, 0f, 0f, null)

                // 3. 绘制当前拖拽中的临时箭头
                currentArrow?.let { drawArrow(this, it) }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 转换触摸坐标到bitmap坐标系
        val adjustedEvent = MotionEvent.obtain(event)
        adjustedEvent.transform(inverseMatrix)
        val x = adjustedEvent.x.coerceIn(0f, bitmap?.width?.toFloat() ?: 0f)
        val y = adjustedEvent.y.coerceIn(0f, bitmap?.height?.toFloat() ?: 0f)

        return when (currentMode) {
            DrawMode.ARROW -> {
                handleArrowTouch(adjustedEvent, x, y)
                true
            }
            else -> {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        currentPath.moveTo(x, y)
                        canvas?.save()
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        currentPath.lineTo(x, y)
                        if (isMosaicMode) drawMosaic(x.toInt(), y.toInt())
                        else drawPathOnBitmap()
                        invalidate()
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        canvas?.restore()
                        if (!isMosaicMode) drawPathOnBitmap()
                        currentPath.reset()
                        notifyBitmapChanged()
                        true
                    }
                    else -> false
                }
            }
        }.also { adjustedEvent.recycle() }
    }

    private fun drawPathOnBitmap() {
        graffitiPaint.xfermode = null
        canvas?.drawPath(currentPath, graffitiPaint)
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

        for (i in startX until endX step blockSize) {
            for (j in startY until endY step blockSize) {
                val avgColor = getAverageColorAroundPoint(
                    i + blockSize / 2,
                    j + blockSize / 2,
                    sampleRadius
                )

                graffitiPaint.color = avgColor

                // 根据样式设置不同的绘制效果
                if (style == MOSAIC_STYLE_BLUR) {
                    graffitiPaint.maskFilter = BlurMaskFilter(blurRadius, blurStyle)
                    Log.d("GraffitiView", "drawMosaic: blurRadius: $blurRadius, blurStyle: $style")
                } else {
                    graffitiPaint.maskFilter = null  // 禁用模糊
                    graffitiPaint.style = Paint.Style.FILL_AND_STROKE
                    Log.d("GraffitiView", "drawMosaic: blurRadius: $blurRadius, blurStyle: $style")
                }

                val drawEndX = minOf(i + blockSize, endX)
                val drawEndY = minOf(j + blockSize, endY)
                canvas?.drawRect(
                    i.toFloat(),
                    j.toFloat(),
                    drawEndX.toFloat(),
                    drawEndY.toFloat(),
                    graffitiPaint
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
                graffitiPaint.strokeWidth = 6f
            }

            1 -> {
                graffitiPaint.strokeWidth = 14f
            }

            2 -> {
                graffitiPaint.strokeWidth = 24f
            }
        }
        invalidate() // 刷新视图以应用新的笔刷大小
    }

    //设置笔刷颜色
    fun setStrokeColor(color: Int) {
        graffitiPaint.color = color

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
        if (style == MOSAIC_STYLE_BLUR){
            maskBlur = maskBlur.coerceIn(10f, 30f)
        }else{
            maskBlur = maskBlur.coerceIn(1f, 50f)

        }

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
    // 新增马赛克样式常量
    companion object {
        const val MOSAIC_STYLE_BLUR = 0   // 模糊马赛克
        const val MOSAIC_STYLE_BLOCK = 1  // 方块马赛克
    }
    fun setMosaicStyle(style: Int) {
        this.style = style
        when (style) {
            MOSAIC_STYLE_BLUR -> {
                blurStyle = BlurMaskFilter.Blur.NORMAL
                mosaicRadius = 5  // 模糊马赛克推荐尺寸
            }
            MOSAIC_STYLE_BLOCK -> {
                mosaicRadius = 8  // 方块马赛克推荐尺寸
            }
        }
        invalidate() // 刷新视图以应用新的马赛克半径

    }

    private fun notifyBitmapChanged() {
        bitmap?.let {
            bitmapChangeListener?.onBitmapChange(it.copy(Bitmap.Config.ARGB_8888, false))
        }
    }

    // 重新绘制所有内容到Bitmap
    private fun redrawAll() {
        bitmap?.eraseColor(Color.TRANSPARENT)
        arrows.forEach { drawArrow(canvas!!, it) }
        invalidate()
    }

}
