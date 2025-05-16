package com.electroboys.lightsnap.ui.main.view;

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.electroboys.lightsnap.R

class GraffitiTabView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var ivMosaicOne: View
    private var ivMosaicTwo: View
    private var ivMosaicThree: View
    private var tvSelectColorView: View
    private var ivStraightLine: View
    private var ivWavyLine: View
    private var listener: OnSelectedListener? = null

    init {
        // 加载自定义布局
        LayoutInflater.from(context).inflate(R.layout.layout_graffiti_tab, this, true)
        // 初始化其他组件或属性
        tvSelectColorView = findViewById(R.id.tv_select_color)
        ivMosaicOne = findViewById(R.id.iv_mosaic_one)
        ivMosaicTwo = findViewById(R.id.iv_mosaic_two)
        ivMosaicThree = findViewById(R.id.iv_mosaic_three)
        ivStraightLine = findViewById(R.id.iv_straight_line)
        ivWavyLine = findViewById(R.id.iv_wavy_line)

        ivStraightLine.isSelected = true
        ivMosaicOne.isSelected = true

        // 设置点击事件
        ivMosaicOne.setOnClickListener {
            listener?.onSelectSize(0)
            updateSelectedView(0)
        }
        ivMosaicTwo.setOnClickListener {
            listener?.onSelectSize(1)
            updateSelectedView(1)
        }
        ivMosaicThree.setOnClickListener {
            listener?.onSelectSize(2)
            updateSelectedView(2)
        }
        findViewById<View>(R.id.tv_white_color).setOnClickListener {
            listener?.onColorSelected(context.getColor(R.color.white))
            tvSelectColorView.setBackgroundColor(context.getColor(R.color.white))
        }
        findViewById<View>(R.id.tv_black_color).setOnClickListener {
            listener?.onColorSelected(context.getColor(R.color.black))
            tvSelectColorView.setBackgroundColor(context.getColor(R.color.black))
        }
        findViewById<View>(R.id.tv_red_color).setOnClickListener {
            listener?.onColorSelected(context.getColor(R.color.red))
            tvSelectColorView.setBackgroundColor(context.getColor(R.color.red))
        }
        findViewById<View>(R.id.tv_green_color).setOnClickListener {
            listener?.onColorSelected(context.getColor(R.color.green))
            tvSelectColorView.setBackgroundColor(context.getColor(R.color.green))
        }
        findViewById<View>(R.id.tv_blue_color).setOnClickListener {
            listener?.onColorSelected(context.getColor(R.color.blue))
            tvSelectColorView.setBackgroundColor(context.getColor(R.color.blue))
        }
        findViewById<View>(R.id.tv_yellow_color).setOnClickListener {
            listener?.onColorSelected(context.getColor(R.color.yellow))
            tvSelectColorView.setBackgroundColor(context.getColor(R.color.yellow))
        }
        findViewById<View>(R.id.tv_purple_color).setOnClickListener {
            listener?.onColorSelected(context.getColor(R.color.purple))
            tvSelectColorView.setBackgroundColor(context.getColor(R.color.purple))
        }

        findViewById<View>(R.id.tv_gray_color).setOnClickListener {
            listener?.onColorSelected(context.getColor(R.color.gray))
            tvSelectColorView.setBackgroundColor(context.getColor(R.color.gray))
        }
        findViewById<View>(R.id.iv_straight_line).setOnClickListener {
            listener?.onLineStyleSelected(0)
            ivStraightLine.isSelected = true
            ivWavyLine.isSelected = false
        }
        findViewById<View>(R.id.iv_wavy_line).setOnClickListener {
            listener?.onLineStyleSelected(1)
            ivWavyLine.isSelected = true
            ivStraightLine.isSelected = false
        }


    }

    private fun updateSelectedView(style: Int) {
        ivMosaicOne.isSelected = false
        ivMosaicThree.isSelected = false
        ivMosaicTwo.isSelected = false
        when (style) {
            0 -> ivMosaicOne.isSelected = true
            1 -> ivMosaicTwo.isSelected = true
            2 -> ivMosaicThree.isSelected = true
        }
    }

    interface OnSelectedListener {
        fun onColorSelected(color: Int)
        fun onSelectSize(size: Int)
        fun onLineStyleSelected(style: Int)
    }

    fun setOnSelectedListener(listener: OnSelectedListener) {
        this.listener = listener
    }
}
