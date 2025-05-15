package com.electroboys.lightsnap.ui.main.view

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.electroboys.lightsnap.R

class ArrowTabView(context: Context) : LinearLayout(context) {

    interface OnArrowStyleSelectedListener {
        fun onColorSelected(color: Int)
        fun onWidthSelected(width: Float)
        fun onStyleSelected(style: Int) // 0: 实线, 1: 虚线
    }

    private var listener: OnArrowStyleSelectedListener? = null
    private var currentColor = Color.RED
    private var currentWidth = 5f
    private var currentStyle = 0 // 0: 实线, 1: 虚线

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_arrow_tab, this, true)

        // 初始化颜色选择器
        val colorViews = listOf(
            findViewById<TextView>(R.id.tv_white_color),
            findViewById<TextView>(R.id.tv_black_color),
            findViewById<TextView>(R.id.tv_red_color),
            findViewById<TextView>(R.id.tv_green_color),
            findViewById<TextView>(R.id.tv_blue_color),
            findViewById<TextView>(R.id.tv_yellow_color),
            findViewById<TextView>(R.id.tv_purple_color),
            findViewById<TextView>(R.id.tv_gray_color)
        )

        val colors = listOf(
            Color.WHITE, Color.BLACK, Color.RED, Color.GREEN,
            Color.BLUE, Color.YELLOW, Color.parseColor("#800080"), Color.GRAY
        )

        colorViews.forEachIndexed { index, view ->
            view.setOnClickListener {
                currentColor = colors[index]
                updateSelectedColor(currentColor)
                listener?.onColorSelected(currentColor)
            }
        }

        // 初始化样式选择器
        findViewById<ImageView>(R.id.iv_arrow_solid).setOnClickListener {
            currentStyle = 0
            updateStyleSelection()
            listener?.onStyleSelected(currentStyle)
        }

        findViewById<ImageView>(R.id.iv_arrow_dashed).setOnClickListener {
            currentStyle = 1
            updateStyleSelection()
            listener?.onStyleSelected(currentStyle)
        }

        // 初始化宽度控制器
        val widthSeekBar = findViewById<SeekBar>(R.id.iv_arrow_width)
        widthSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                currentWidth = progress.toFloat()
                listener?.onWidthSelected(currentWidth)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // 可选的开始触摸事件处理
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // 可选的结束触摸事件处理
            }
        })

        // 设置默认选择
        widthSeekBar.progress = currentWidth.toInt()
        updateSelectedColor(currentColor)
        updateStyleSelection()
    }

    private fun updateSelectedColor(color: Int) {
        findViewById<TextView>(R.id.tv_select_color).setBackgroundColor(color)
    }

    private fun updateStyleSelection() {
        findViewById<ImageView>(R.id.iv_arrow_solid).isSelected = currentStyle == 0
        findViewById<ImageView>(R.id.iv_arrow_dashed).isSelected = currentStyle == 1
    }

    fun setOnArrowStyleSelectedListener(listener: OnArrowStyleSelectedListener) {
        this.listener = listener
    }
}