package com.electroboys.lightsnap.domain.screenshot

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.ToggleButton

// 协调各个组件的工作
class EditScreenshot(
    context: Context,
    container: ViewGroup
){
    private var editAddTextView: EditAddTextView = EditAddTextView(context)
    private lateinit var textController: AddTextController

    init {
        container.addView(editAddTextView, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
    }
    fun addText(
        btnText: ImageButton,
        btnConfirmText: ImageButton,
        btnCancelText: ImageButton,
        btnIsBold: ImageButton,
        btnColorPicker: Button? = null,
        textSizeSeekBar: SeekBar,
        btnAddTextDone: ImageButton,
        textInput: EditText
    ){
        textController = AddTextController(
            editAddTextView = editAddTextView,
            btnText = btnText,
            btnConfirmText = btnConfirmText,
            btnCancelText = btnCancelText,
            btnIsBold = btnIsBold,
            btnColorPicker = btnColorPicker,
            textSizeSeekBar = textSizeSeekBar,
            btnAddTextDone = btnAddTextDone,
            textInput = textInput
        )
    }
    fun getFinalBitmap(orignalBitmap: Bitmap): Bitmap{
        return editAddTextView.getFinalBitmap(orignalBitmap)
    }
    fun clearAllText(){
        editAddTextView.clearAllText()
    }
    fun setTypeface(typeface: Typeface){
        textController.setTypeface(typeface)
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