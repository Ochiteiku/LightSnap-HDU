package com.electroboys.lightsnap.domain.screenshot.modehandler

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.data.screenshot.BitmapCache
import com.electroboys.lightsnap.data.screenshot.ImageHistory
import com.electroboys.lightsnap.domain.screenshot.ControlModeHandler
import com.electroboys.lightsnap.ui.main.activity.ScreenshotActivity.Companion.EXTRA_SCREENSHOT_KEY
import com.electroboys.lightsnap.ui.main.view.EditAddTextBarView
import com.electroboys.lightsnap.ui.main.view.EditAddTextView
import com.electroboys.lightsnap.ui.main.view.FrameSelectView
import com.electroboys.lightsnap.ui.main.view.GraffitiView
import yuku.ambilwarna.AmbilWarnaDialog

class AddTextModeHandler(
    private val context: Context,
    private val intent: Intent,
    private val imageView: ImageView,
    private val exControlFrame: FrameLayout,
    private val btnText: ImageButton,
    private val container: ViewGroup
) : ControlModeHandler {

    private val editAddTextView = EditAddTextView(context)
    private val editAddTextBarView = EditAddTextBarView(context)

    private var isBold = false
    private var isItalic = false
    private var currentTextSize = 40f
    private var currentTextColor = Color.RED
    private var typeface: Typeface? = null
    private var isColorpicker = true
    private var isActive = false

    override fun apply(
        context: Context,
        bitmap: Bitmap,
        graffitiView: GraffitiView,
        frameSelectView: FrameSelectView,
        exControlFrame: ViewGroup
    ) {
        frameSelectView.visibility = View.GONE
        graffitiView.visibility = View.GONE

        if (editAddTextView.parent == null) {
            container.addView(editAddTextView)
        }

        exControlFrame.removeAllViews()
        exControlFrame.addView(editAddTextBarView)
        editAddTextView.visibility = View.VISIBLE
        editAddTextBarView.visibility = View.VISIBLE

        setupListeners()
        isActive = true
    }

    fun exit() {
        editAddTextView.clearAllText()
        editAddTextView.visibility = View.GONE
        editAddTextBarView.visibility = View.GONE
        editAddTextBarView.clearState()
        isActive = false
    }

    private fun setupListeners() {
        editAddTextBarView.apply {
            btnIsBoldlistener = {
                isBold = !isBold
                updateTextProps()
                isBold
            }
            btnIsItalicistener = {
                isItalic = !isItalic
                updateTextProps()
                isItalic
            }
            btnColorPickerlistener = {
                isColorpicker = !isColorpicker
                showColorPickerDialog()
                isColorpicker
            }
            textSizeSeekBarlistener = {
                currentTextSize = it
                updateTextProps()
            }
            textInputlistener = { text ->
                editAddTextView.startAddingText(
                    text = text,
                    size = currentTextSize,
                    color = currentTextColor,
                    isBold = isBold,
                    isItalic = isItalic,
                    typeface = typeface
                )
            }
            fontPickerlistener = { position ->
                val font = context.resources.getStringArray(R.array.systemFonts)[position]
                typeface = Typeface.create(font, Typeface.NORMAL)
                updateTextProps()
            }
            btnAddTextDonelistener = {
                editAddTextView.addTextDone()
                applyTextToBitmap()
                exit()
            }
        }
    }

    private fun updateTextProps() {
        editAddTextView.setCurrentTextProperties(
            size = currentTextSize,
            color = currentTextColor,
            isBold = isBold,
            isItalic = isItalic,
            typeface = typeface
        )
    }

    private fun showColorPickerDialog() {
        val dialog = AmbilWarnaDialog(context, currentTextColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog?) {}
            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                currentTextColor = color
                updateTextProps()
            }
        })
        dialog.dialog.window?.apply {
            setGravity(Gravity.CENTER)
            setBackgroundDrawableResource(R.drawable.bg_addtext_colorpicker_dialog)
        }
        dialog.show()
    }

    private fun applyTextToBitmap() {
        val originalKey = intent.getStringExtra(EXTRA_SCREENSHOT_KEY)
        val originalBitmap = originalKey?.let { BitmapCache.getBitmap(it) }
        if (originalBitmap != null) {
            val resultBitmap = editAddTextView.getFinalBitmap(originalBitmap, imageView)
            val newKey = BitmapCache.cacheBitmap(resultBitmap)
            intent.putExtra(EXTRA_SCREENSHOT_KEY, newKey)
            ImageHistory.push(newKey)
            imageView.setImageBitmap(resultBitmap)
        }
    }
}

