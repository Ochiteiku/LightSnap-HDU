package com.electroboys.lightsnap.domain.screenshot

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast

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

    data class TextItem(
        var text: String?,
        var x: Float,
        var y: Float,
        var size: Float,
        var color: Int,
        var isBold: Boolean,
        var typeface: Typeface?
    )

    fun startAddingText(
        text: String?,
        size: Float,
        color: Int,
        isBold: Boolean,
        typeface: Typeface?
    ){
        currentItem = TextItem(
            text = text,
            x = width / 2f,
            y = height / 2f,
            size = size,
            color = color,
            isBold = isBold,
            typeface = typeface
        )
        isAddingText = true
        Toast.makeText(context, "点击屏幕中的任意位置可添加文本", Toast.LENGTH_SHORT).show()
        // requestFocus()
        // invalidate()
    }

    fun setCurrentTextProperties(
        size: Float,
        color: Int,
        isBold: Boolean,
        typeface: Typeface?
    ){
        currentItem?.let{
            it.size = size
            it.isBold = isBold
            it.color = color
            it.typeface = typeface
            // 请求系统在下一个绘制周期重绘该视图
            // 触发 onDraw(Canvas) 方法的调用
            invalidate()
        }
    }

    // 确认文字
    fun addTextDone(){
        currentItem?.let {
            textItems.add(it)
        }
        currentItem = null
        isAddingText = false
        invalidate()
    }

    fun cancelText(){
        currentItem = null
        isAddingText = false
        invalidate()
    }

    fun clearAllText(){
        textItems.clear()
        invalidate()
    }

    fun getFinalBitmap(originalBitmap: Bitmap): Bitmap{
        val bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888,true)
        val canvas = Canvas(bitmap)
        drawAllTextItems(canvas)
        return bitmap
    }

    // 获取当前添加的所有文字
    fun getTextItems(): List<TextItem> = textItems.toList()

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(!isAddingText) return false

        when(event?.action){
            MotionEvent.ACTION_DOWN -> {
                // 点击创建新的文本项
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
            canvas.drawText(it.text!!, it.x, it.y, paint)
        }
    }

    private fun drawAllTextItems(canvas: Canvas){
        textItems.forEach{
            item ->
            val paint = createTextPaint(item, 255)
            canvas.drawText(item.text!!, item.x, item.y, paint)
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
                item.typeface != null -> item.typeface
                item.isBold -> Typeface.DEFAULT_BOLD
                else -> Typeface.DEFAULT
            }
        }
    }
}