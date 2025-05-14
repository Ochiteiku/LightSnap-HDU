package com.electroboys.lightsnap.ui.main.view;

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.electroboys.lightsnap.R

class FrameSelectTabView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var ivFrameOne: View
    private var ivFrameTwo: View
    private var ivFrameThree: View
    private var tvSelectColorView: View
    private var ivFrameStyleOne: View
    private var ivFrameStyleTwo: View

    private var listener: OnSelectedListener? = null

    init {
        // 加载自定义布局
        LayoutInflater.from(context).inflate(R.layout.layout_fragment_select_tab, this, true)
        // 初始化其他组件或属性
        tvSelectColorView = findViewById(R.id.tv_select_color)
        ivFrameOne = findViewById(R.id.iv_frame_one)
        ivFrameTwo = findViewById(R.id.iv_frame_two)
        ivFrameThree = findViewById(R.id.iv_frame_three)
        ivFrameStyleOne = findViewById(R.id.iv_frame_style_one)
        ivFrameStyleTwo = findViewById(R.id.iv_frame_style_two)
        ivFrameOne.isSelected = true
        ivFrameStyleOne.isSelected = true
        // 设置点击事件
        ivFrameOne.setOnClickListener {
            listener?.onSelectSize(0)
            updateSelectedView(0)
        }
        ivFrameTwo.setOnClickListener {
            listener?.onSelectSize(1)
            updateSelectedView(1)
        }
        ivFrameThree.setOnClickListener {
            listener?.onSelectSize(2)
            updateSelectedView(2)
        }
        ivFrameStyleTwo.setOnClickListener {
            listener?.OnSelectedStyle(1)
            ivFrameStyleOne.isSelected = false
            ivFrameStyleTwo.isSelected = true
        }
        ivFrameStyleOne.setOnClickListener {
            listener?.OnSelectedStyle(0)
            ivFrameStyleOne.isSelected = true
            ivFrameStyleTwo.isSelected = false
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



    }

    private fun updateSelectedView(style: Int) {
        ivFrameOne.isSelected = false
        ivFrameThree.isSelected = false
        ivFrameTwo.isSelected = false
        when (style) {
            0 -> ivFrameOne.isSelected = true
            1 -> ivFrameTwo.isSelected = true
            2 -> ivFrameThree.isSelected = true
        }
    }

    interface OnSelectedListener {
        fun onColorSelected(color: Int)
        fun onSelectSize(size: Int)
        fun OnSelectedStyle(style: Int)
    }

    fun setOnSelectedListener(listener: OnSelectedListener) {
        this.listener = listener
    }
}
