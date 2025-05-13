package com.electroboys.lightsnap.domain.screenshot

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.ui.main.view.EditAddTextBarView
import com.electroboys.lightsnap.ui.main.view.EditAddTextView

// 协调各个组件的工作
class EditScreenshot(
    context: Context,
    private var container: ViewGroup
){
    private var editAddTextView: EditAddTextView = EditAddTextView(context)
    private var editAddTextBarView: EditAddTextBarView = EditAddTextBarView(context)
    private var currentText: String? = null
    private var currentTextSize = 40f
    private var currentTextColor = Color.RED
    private var isBold = false
    private var isItalic = false
    private var typeface: Typeface? = null

    fun addText(
        btnText: ImageButton,
        exControlFrame: FrameLayout
    ) {
        btnText.setImageResource(R.drawable.ic_addtext_textboxfilled)
        // 创建editaddingtextbar，并设置监听器
        editAddTextBarView.apply {
            btnAddTextDonelistener = {
                editAddTextView.addTextDone()
                btnText.setImageResource(R.drawable.ic_addtext_textbox)
            }

            btnCancellistener = {
                editAddTextView.cancelText()
                btnText.setImageResource(R.drawable.ic_addtext_textbox)
            }

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
        exControlFrame.addView(editAddTextBarView)
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

    fun setTypeface(typeface: Typeface){
        this.typeface = typeface
        updateCurrentTextProperties()
    }

    fun getFinalBitmap(orignalBitmap: Bitmap): Bitmap{
        return editAddTextView.getFinalBitmap(orignalBitmap)
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