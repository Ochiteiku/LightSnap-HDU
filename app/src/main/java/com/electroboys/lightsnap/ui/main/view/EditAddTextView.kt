package com.electroboys.lightsnap.ui.main.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView

// 负责绘制以及触摸控制
class EditAddTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs)  {

    // view中包含的多个文本项
    private var textItems = mutableListOf<TextItem>()
    private var isAddingText: Boolean = false

    // 设置EditText的默认值
    private var currentItem: TextItem? = null  // 当前的编辑文本项（包含颜色、大小等）

    var BitmapChangeListener: ((Bitmap) -> Unit)? = null
    var onCompleteTextEdit: (() -> Unit)? = null

    data class TextItem(
        var text: String?,
        var x: Float,
        var y: Float,
        var size: Float,
        var color: Int,
        var isBold: Boolean,
        var isItalic: Boolean,
        var typeface: Typeface?
    )

    fun startAddingText(
        text: String?,
        size: Float,
        color: Int,
        isBold: Boolean,
        isItalic: Boolean,
        typeface: Typeface?
    ){
        currentItem = TextItem(
            text = text,
            x = width / 2f,
            y = height / 2f,
            size = size,
            color = color,
            isBold = isBold,
            isItalic = isItalic,
            typeface = typeface
        )
        isAddingText = true
        // Toast.makeText(context, "点击屏幕中的任意位置可添加文本", Toast.LENGTH_SHORT).show()
        // requestFocus()
        invalidate()
    }

    fun setCurrentTextProperties(
        size: Float,
        color: Int,
        isBold: Boolean,
        isItalic: Boolean,
        typeface: Typeface?
    ){
        currentItem?.let{
            it.size = size
            it.isBold = isBold
            it.isItalic = isItalic
            it.color = color
            it.typeface = typeface
            // 请求系统在下一个绘制周期重绘该视图
            // 触发 onDraw(Canvas) 方法的调用
            invalidate()
        }
    }

    // 确认文字
    fun addTextDone(){
        val item = currentItem ?: return

        // 如果没有拖动或点击就默认居中显示
        if (item.x == 0f && item.y == 0f) {
            item.x = width / 2f
            item.y = height / 2f
        }
        textItems.add(item)
        currentItem = null
        isAddingText = false
        invalidate()
    }

    fun clearAllText(){
        textItems.clear()
        invalidate()
    }

    // 获取当前添加的所有文字
    fun getTextItems(): List<TextItem> = textItems.toList()

    fun getFinalBitmap(originalBitmap: Bitmap, imageView: ImageView): Bitmap {
        val bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)

        // 获取 ImageView 的变换矩阵
        val matrix = imageView.imageMatrix
        val values = FloatArray(9).apply { matrix.getValues(this) }

        val scaleX = values[Matrix.MSCALE_X]
        val scaleY = values[Matrix.MSCALE_Y]
        val transX = values[Matrix.MTRANS_X]
        val transY = values[Matrix.MTRANS_Y]

        textItems.forEach { item ->
            // 根据缩放比例调整字体大小
            val scaledTextSize = item.size / scaleX

            val paint = createTextPaint(item, 255).apply {
                textSize = scaledTextSize
            }

            val fontMetrics = paint.fontMetrics
            val baselineOffset = (fontMetrics.ascent + fontMetrics.descent) / 2

            // 将 EditAddTextView 坐标转换为原始图片坐标
            val mappedX = (item.x - transX) / scaleX
            val mappedY = (item.y - transY) / scaleY

            canvas.drawText(item.text!!, mappedX, mappedY - baselineOffset, paint)
        }

        return bitmap
    }


    private fun drawAllTextItems(canvas: Canvas){
        textItems.forEach{
                item ->
            val paint = createTextPaint(item, 255)
            val fontMetrics = paint.fontMetrics
            val baselineOffset = (fontMetrics.ascent + fontMetrics.descent) / 2 // 计算基线偏移量
            canvas.drawText(item.text!!, item.x, item.y - baselineOffset, paint)
        }
    }

    private fun createTextPaint(
        item: TextItem,
        alpha: Int
    ): Paint{
        return Paint().apply {
            color = item.color
            textSize = item.size
            isAntiAlias = true
            this.alpha =alpha
            typeface = when {
                item.isBold && item.isItalic ->
                    if(item.typeface != null) {
                        Typeface.create(item.typeface, Typeface.BOLD_ITALIC)
                    }else{
                        Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                    }
                item.isBold ->
                    if(item.typeface != null) {
                        Typeface.create(item.typeface, Typeface.BOLD)
                    }else{
                        Typeface.DEFAULT_BOLD
                    }
                item.isItalic ->
                    if(item.typeface != null) {
                        Typeface.create(item.typeface, Typeface.ITALIC)
                    }else{
                        Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    }
                item.typeface != null -> item.typeface
                else -> Typeface.DEFAULT
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(!isAddingText) return false

        when(event?.action){
            MotionEvent.ACTION_DOWN -> {
                currentItem?.let {
                    it.x = event.x
                    it.y = event.y
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                currentItem?.let {
                    it.x = event.x
                    it.y = event.y
                    invalidate()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 每次都要绘制一遍？可以改进吗
        drawAllTextItems(canvas)

        // 绘制当前正在添加的文字
        currentItem?.let {
            val paint = createTextPaint(it, 150)
            val fontMetrics = paint.fontMetrics
            val baselineOffset = (fontMetrics.ascent + fontMetrics.descent) / 2
            canvas.drawText(it.text!!, it.x, it.y - baselineOffset, paint)
        }
    }

}