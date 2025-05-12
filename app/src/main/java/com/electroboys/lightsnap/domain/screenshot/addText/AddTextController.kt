package com.electroboys.lightsnap.domain.screenshot.addText

import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.core.view.isVisible
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.ui.main.view.EditAddTextView

// 处理相应按钮控制逻辑
class AddTextController (
    private val editAddTextView: EditAddTextView,
    private val btnText: ImageButton,
    private val btnConfirmText: ImageButton,
    private val btnCancelText: ImageButton,
    private val btnIsBold: ImageButton,
    private val btnColorPicker: Button? = null,
    private val textSizeSeekBar: SeekBar,
    private val btnAddTextDone: ImageButton,
    private val textInput: EditText
){
    // private var currentEditText: AppCompatEditText? = null
    private var currentText: String? = null
    private var currentTextSize = 40f
    private var currentTextColor = Color.RED
    private var isBold = false
    private var typeface: Typeface? = null

    init {
        setupListeners()
        updateUIState(false)
    }

    private fun setupListeners(){
        btnText.setOnClickListener{
            btnText.setImageResource(R.drawable.ic_addtext_textboxfilled)
            startTextAddingMode()
        }
        btnIsBold.setOnClickListener{
            isBold = !isBold
            if(isBold){
                // 需要加粗
                btnIsBold.setImageResource(R.drawable.ic_addtext_isbold)
                updateCurrentTextProperties()
            }else{
                btnIsBold.setImageResource(R.drawable.ic_addtext_unbold)
                updateCurrentTextProperties()
            }
        }
        btnConfirmText.setOnClickListener{
            confirmTextAdding()
        }
        btnCancelText.setOnClickListener{
            cancelTextAdding()
        }
        textSizeSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentTextSize = progress.toFloat()
                updateCurrentTextProperties()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        btnColorPicker?.setOnClickListener{
            //TODO 颜色选择器
            updateCurrentTextProperties()
        }
        btnAddTextDone.setOnClickListener{
            addTextDone()
        }
    }

    private fun startTextAddingMode(){
        updateUIState(true)
    }

    private fun confirmTextAdding(){
        // 将赋值逻辑添加到确定按钮点击事件中
        Log.d("AddTextController","confirmTextAdding读取文本textInput赋值给currentText")
        currentText = textInput.text.toString()
        editAddTextView.startAddingText(
            text = currentText,
            size = currentTextSize,
            color = currentTextColor,
            isBold = isBold,
            typeface = typeface
        )
    }
    private fun addTextDone(){
        editAddTextView.addTextDone()
        updateUIState(false)
    }
    private fun cancelTextAdding(){
        editAddTextView.cancelText()
        updateUIState(false)
    }
    private fun updateCurrentTextProperties(){
        editAddTextView.setCurrentTextProperties(
            size = currentTextSize,
            color = currentTextColor,
            isBold = isBold,
            typeface = typeface
        )
    }
    private fun updateUIState(isAddingText: Boolean){
        btnColorPicker?.isVisible = isAddingText
        btnConfirmText.isVisible = isAddingText
        btnCancelText.isVisible = isAddingText
        btnIsBold.isVisible = isAddingText
        textSizeSeekBar.isVisible = isAddingText
        textInput.isVisible = isAddingText
        btnAddTextDone.isVisible = isAddingText
    }

    fun setTypeface(typeface: Typeface){
        this.typeface = typeface
        updateCurrentTextProperties()
    }

    //    旧的代码逻辑
//    private fun constructEditText(){
//        currentEditText?.let { container.removeView(it) }
//        Log.d("EditAddTextView", "创建edittext组件")
//        // 创建edittext组件
//        val editText = object: AppCompatEditText(context){
//            override fun onEditorAction(actionCode: Int) {
//                super.onEditorAction(actionCode)
//                if(actionCode == android.view.inputmethod.EditorInfo.IME_ACTION_DONE){
//                    saveTextAndHideKeyboard()
//                }
//            }
//        }.apply {
//            layoutParams = LayoutParams(
//                LayoutParams.WRAP_CONTENT,
//                LayoutParams.WRAP_CONTENT
//            ).apply {
//                leftMargin = width / 2
//                topMargin = height / 2
//            }
//
//            setBackgroundColor(Color.TRANSPARENT)
//            setTextColor(currentTextColor)
//            textSize = currentTextSize
//            imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_DONE
//            setSingleLine(true)
//            setOnFocusChangeListener{ _, hasFocus ->
//                Log.d("EditAddTextView", "监听到edittext的focus事件")
//                if(!hasFocus){
//                    Log.d("EditAddTextView", "即将存储文本saveTextAndHideKeyboard")
//                    saveTextAndHideKeyboard()
//                }
//            }
//        }
//
//        container.addView(editText)
//        editText.requestFocus()
//
//        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
//
//        currentEditText = editText
//    }
//
//    private fun saveTextAndHideKeyboard(){
//        // 将获取到的文本传入currentText中
//        currentEditText?.let {
//                editText ->
////            val text = editText.text.toString()
////            Log.d("EditAddTextView", "待存储的text = ${text}")
////            if(text.isNotEmpty()){
////                currentItem?.text = text
////                textItems.add(currentItem!!)
////                invalidate()
////            }
//            // 此时将获取的text传入
//            currentText = currentEditText?.text.toString()
//            container.removeView(editText)
//            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.hideSoftInputFromWindow(editText.windowToken, 0)
//            currentEditText = null
//        }
//    }
}