package com.electroboys.lightsnap.ui.main.view

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.core.view.isVisible
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.domain.screenshot.watermark.WatermarkConfig

class WatermarkSettingBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var etWatermarkText: EditText
    private var sbWatermarkAlpha: SeekBar

    var onTextChanged: ((String) -> Unit)? = null
    var onAlphaChanged: ((Int) -> Unit)? = null
    var onConfirmWatermark: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_watermark_setting_bar, this, true)

        etWatermarkText = findViewById(R.id.watermark_text)
        sbWatermarkAlpha = findViewById(R.id.watermark_alpha)

        // 文字输入监听
        etWatermarkText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                onTextChanged?.invoke(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 不透明度变化监听
        sbWatermarkAlpha.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                onAlphaChanged?.invoke(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        findViewById<ImageButton>(R.id.btnConfirm).setOnClickListener {
            onConfirmWatermark?.invoke()
        }

    }

    // 更新 UI 状态（显示/隐藏）
    fun updateUIState(isVisible: Boolean) {
        etWatermarkText.isVisible = isVisible
        sbWatermarkAlpha.isVisible = isVisible
    }

    // 设置当前配置
    fun setConfig(config: WatermarkConfig) {
        etWatermarkText.setText(config.text)
        sbWatermarkAlpha.progress = config.alpha
    }
}
