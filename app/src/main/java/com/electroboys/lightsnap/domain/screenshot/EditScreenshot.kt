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

    private var currentText: String? = null
    private var currentTextSize = 40f
    private var currentTextColor = Color.RED
    private var isBold = false
    private var isItalic = false
    private var isColorpicker = true
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
        val finalBitmap = getFinalBitmap(originalBitmap!!)
        // 将最新的bitmap放入缓存中
        var newKey = BitmapCache.cacheBitmap(finalBitmap)
        Log.d("ScreenshotActivity", "${intent.getStringExtra(EXTRA_SCREENSHOT_KEY)},${newKey}")
        intent.putExtra(EXTRA_SCREENSHOT_KEY, newKey)
        ImageHistory.push(newKey)
        imageView.setImageBitmap(BitmapCache.getBitmap(newKey))
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
    fun getFinalBitmap(orignalBitmap: Bitmap): Bitmap{
        return editAddTextView.getFinalBitmap(orignalBitmap)
    }

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