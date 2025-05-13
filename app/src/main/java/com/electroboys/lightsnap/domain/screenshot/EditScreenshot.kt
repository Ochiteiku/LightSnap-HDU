package com.electroboys.lightsnap.domain.screenshot

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.data.screenshot.BitmapCache
import com.electroboys.lightsnap.data.screenshot.ImageHistory
import com.electroboys.lightsnap.ui.main.activity.ScreenshotActivity.Companion.EXTRA_SCREENSHOT_KEY
import com.electroboys.lightsnap.ui.main.view.EditAddTextBarView
import com.electroboys.lightsnap.ui.main.view.EditAddTextView

// 协调各个组件的工作
class EditScreenshot(
    context: Context,
    private var container: ViewGroup,
    private var intent: Intent
){
    private var editAddTextView: EditAddTextView = EditAddTextView(context)
    private var editAddTextBarView: EditAddTextBarView = EditAddTextBarView(context)

    private var currentText: String? = null
    private var currentTextSize = 40f
    private var currentTextColor = Color.RED
    private var isBold = false
    private var isItalic = false
    private var typeface: Typeface? = null

    init {
        editAddTextBarView.apply {

            btnIsBoldlistener = {
                isBold = !isBold
                updateCurrentTextProperties()
                isBold
            }

            btnIsItalicistener = {
                isItalic = !isItalic
                updateCurrentTextProperties()
                isItalic
            }

            btnColorPickerlistener = {
                //TODO
            }

            textSizeSeekBarlistener = {
                    textsize ->
                currentTextSize = textsize
                updateCurrentTextProperties()
            }

            textInputlistener = {
                    textInput ->
                currentText = textInput
                editAddTextView.startAddingText(
                    text = currentText,
                    size = currentTextSize,
                    color = currentTextColor,
                    isBold = isBold,
                    isItalic = isItalic,
                    typeface = typeface
                )
            }
        }

        container.addView(editAddTextView, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
    }

    fun addText(
        btnText: ImageButton,
        exControlFrame: FrameLayout,
        imageView: ImageView
    ){
        btnText.setImageResource(R.drawable.ic_addtext_textboxfilled)

        // 设置btnAddTextDone的监听器并加入到root界面
        editAddTextBarView.apply {
            btnAddTextDonelistener = {
                editAddTextView.addTextDone()
                bitmapChanged(imageView)
                clearAllText()
                btnText.setImageResource(R.drawable.ic_addtext_textbox)
            }
        }
        exControlFrame.addView(editAddTextBarView)
    }
    fun bitmapChanged(imageView: ImageView){
        // 获取最终的bitmap
        val originalKey = intent.getStringExtra(EXTRA_SCREENSHOT_KEY)
        val originalBitmap = originalKey?.let { BitmapCache.getBitmap(it) }
        if (originalBitmap != null) {
            val finalBitmap = editAddTextView.getFinalBitmap(originalBitmap, imageView) // ✅ 添加 imageView 参数
            val newKey = BitmapCache.cacheBitmap(finalBitmap)
            intent.putExtra(EXTRA_SCREENSHOT_KEY, newKey)
            ImageHistory.push(newKey)
            imageView.setImageBitmap(finalBitmap)
        }
    }

    private fun updateCurrentTextProperties(){
        editAddTextView.setCurrentTextProperties(
            size = currentTextSize,
            color = currentTextColor,
            isBold = isBold,
            isItalic = isItalic,
            typeface = typeface
        )
    }
//    fun getFinalBitmap(orignalBitmap: Bitmap): Bitmap{
//        return editAddTextView.getFinalBitmap(orignalBitmap)
//    }

    fun setTypeface(typeface: Typeface){
        this.typeface = typeface
        updateCurrentTextProperties()
    }

    fun clearAllText(){
        editAddTextView.clearAllText()
    }

    fun addMosic(){

    }
    fun addBox(){

    }
    fun addLine(){

    }
    fun addArrow(){

    }
}