package com.electroboys.lightsnap.domain.screenshot

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.data.screenshot.BitmapCache
import com.electroboys.lightsnap.data.screenshot.ImageHistory
import com.electroboys.lightsnap.ui.main.activity.ScreenshotActivity.Companion.EXTRA_SCREENSHOT_KEY
import com.electroboys.lightsnap.ui.main.view.EditAddTextBarView
import com.electroboys.lightsnap.ui.main.view.EditAddTextView
import yuku.ambilwarna.AmbilWarnaDialog

// 协调各个组件的工作
class EditScreenshot(
    private var context: Context,
    private var container: ViewGroup,
    private var intent: Intent
){
    private var editAddTextView: EditAddTextView = EditAddTextView(context)
    private var editAddTextBarView: EditAddTextBarView = EditAddTextBarView(context)

    companion object {
         val systemFonts = arrayOf(
            "sans-serif",           // 默认
            "sans-serif-light",     // 细体
            "casual",               // 休闲
            "cursive"               // 手写
        )
    }

    // 当前文本的编辑项
    private var currentText: String? = null
    private var currentTextSize = 40f
    private var currentTextColor = Color.RED
    private var isBold = false
    private var isItalic = false
    private var isColorpicker = true
    private var typeface: Typeface? = null

    private var isAddingTextFlag = 0

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
                isColorpicker = !isColorpicker
                showColorPickerDialog()
                isColorpicker
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

            fontPickerlistener = {
                position: Int ->
                val SelectedFont = systemFonts[position]
                typeface = Typeface.create(SelectedFont, Typeface.NORMAL)
                updateCurrentTextProperties()
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
        if(isAddingTextFlag == 0){
            // 第一次进入编辑文本状态
            // 设置监听器 以及 加入父容器中
            btnText.setImageResource(R.drawable.ic_addtext_textboxfilled)
            editAddTextBarView.apply {
                btnAddTextDonelistener = {
                    editAddTextView.addTextDone()
                    bitmapChanged(imageView)
                    clearAllText()
                    btnText.setImageResource(R.drawable.ic_addtext_textbox)
                    updateUIState(false)
                }
            }
            exControlFrame.addView(editAddTextBarView)
        }else if(isAddingTextFlag % 2 == 1){
            // 2、4、6...点击btnText 隐藏addtextbar
            btnText.setImageResource(R.drawable.ic_addtext_textbox)
            editAddTextBarView.updateUIState(false)
        }else{
            // 3、5、7...点击btnText 显示addtextbar
            btnText.setImageResource(R.drawable.ic_addtext_textboxfilled)
            editAddTextBarView.updateUIState(true)
        }
        isAddingTextFlag++
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

    private fun showColorPickerDialog(){
        // 创建颜色选择器对话框
        val colorpickerDialog = AmbilWarnaDialog(context, currentTextColor, object: AmbilWarnaDialog.OnAmbilWarnaListener{
            override fun onCancel(dialog: AmbilWarnaDialog?) { }

            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                currentTextColor = color
                updateCurrentTextProperties()
            }
        })
        // 设置窗口
        colorpickerDialog.dialog.window?.apply{
            setGravity(Gravity.CENTER)
            setBackgroundDrawableResource(R.drawable.bg_addtext_colorpicker_dialog)
            setTitle("颜色选择")
        }
        colorpickerDialog.show()
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

    fun clearAllText(){
        editAddTextView.clearAllText()
    }
}