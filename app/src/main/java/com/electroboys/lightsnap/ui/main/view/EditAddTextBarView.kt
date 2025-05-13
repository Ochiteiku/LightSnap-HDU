package com.electroboys.lightsnap.ui.main.view

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import androidx.core.view.isVisible
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.domain.screenshot.EditScreenshot.Companion.systemFonts

class EditAddTextBarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var btnIsBold: ImageButton
    private var btnIsItalic: ImageButton
    private var btnColorPicker: ImageButton
    private var textSizeSeekBar: SeekBar
    private var btnAddTextDone: ImageButton
    private var textInput: EditText
    private var fontPicker: Spinner

    var btnIsBoldlistener: (() -> Boolean)? = null
    var btnIsItalicistener: (() -> Boolean)? = null
    var btnColorPickerlistener: (() -> Boolean)? = null
    var btnAddTextDonelistener: (() -> Unit)? = null
    var textSizeSeekBarlistener: ((Float) -> Unit)? = null
    var textInputlistener: ((String) -> Unit)? = null
    var fontPickerlistener: ((position: Int) -> Unit)? = null


    init {
        // 加载自定义布局
        LayoutInflater.from(context).inflate(R.layout.layout_addtext_toolbar, this, true)

        // 设置监听器
        btnIsBold = findViewById(R.id.btnIsBold)
        btnIsBold.setOnClickListener{
            var isBold = btnIsBoldlistener?.invoke()
            if(isBold!!){
                btnIsBold.setImageResource(R.drawable.ic_addtext_isbold)
            }else{
                btnIsBold.setImageResource(R.drawable.ic_addtext_unbold)
            }
        }
        btnIsItalic = findViewById(R.id.btnIsItalic)
        btnIsItalic.setOnClickListener{
            var isItalic = btnIsItalicistener?.invoke()
            if(isItalic!!){
                btnIsItalic.setImageResource(R.drawable.ic_addtext_italic)
            }else{
                btnIsItalic.setImageResource(R.drawable.ic_addtext_unitalic)
            }
        }

        btnColorPicker = findViewById(R.id.btnColor)
        btnColorPicker.setOnClickListener{
            var isColorPicker = btnColorPickerlistener?.invoke()
            if(isColorPicker!!){
                btnColorPicker.setImageResource(R.drawable.ic_addtext_uncolorpick)
            }else{
                btnColorPicker.setImageResource(R.drawable.ic_addtext_colorpick)
            }
        }

        textSizeSeekBar = findViewById(R.id.textSizeSeekBar)
        textSizeSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textSizeSeekBarlistener?.invoke(progress.toFloat())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
        )

        btnAddTextDone = findViewById(R.id.btnAddTextDone)
        btnAddTextDone.setOnClickListener{
            btnAddTextDonelistener?.invoke()
        }

        textInput = findViewById(R.id.textInput)
        textInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 输入前的文本状态
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 输入时的实时回调
            }

            override fun afterTextChanged(s: Editable?) {
                // 输入完成后的处理（如验证格式）
                textInputlistener?.invoke(s.toString())
            }
        })

        fontPicker = findViewById(R.id.fontPicker)
        // 利用系统字体列表，设置Spinner适配器
        val adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            systemFonts
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        fontPicker.adapter = adapter

        // 设置Spinner选择监听
        fontPicker.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ){
                fontPickerlistener?.invoke(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

    }

    fun updateUIState(isAddingText: Boolean){
        btnColorPicker.isVisible = isAddingText
        btnIsBold.isVisible = isAddingText
        btnIsItalic.isVisible = isAddingText
        textSizeSeekBar.isVisible = isAddingText
        textInput.isVisible = isAddingText
        btnAddTextDone.isVisible = isAddingText
        fontPicker.isVisible = isAddingText
    }
}