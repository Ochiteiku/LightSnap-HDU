package com.electroboys.lightsnap.ui.main.view;

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import com.electroboys.lightsnap.R

class MosaicTabView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var sbMosaicBlur: SeekBar
    private var ivMosaicOne: View
    private var ivMosaicTwo: View
    private var ivMosaicThree: View
    private var mListener: OnMosaicTabClickListener? = null

    fun setOnMosaicTabClickListener(listener: OnMosaicTabClickListener) {
        mListener = listener
    }

    init {
        // 加载自定义布局
        LayoutInflater.from(context).inflate(R.layout.layout_mosaic_tab, this, true)
        // 初始化其他组件或属性
        ivMosaicOne = findViewById(R.id.iv_mosaic_one)
        ivMosaicTwo = findViewById(R.id.iv_mosaic_two)
        ivMosaicThree = findViewById(R.id.iv_mosaic_three)
        sbMosaicBlur = findViewById(R.id.sb_mosaic_blur)
        ivMosaicOne.isSelected = true
        sbMosaicBlur.progress = 50
        // 设置点击事件
        ivMosaicOne.setOnClickListener {
            mListener?.onMosaicSelectedClick(0)
            updateSelectedView(0)
        }
        ivMosaicTwo.setOnClickListener {
            mListener?.onMosaicSelectedClick(1)
            updateSelectedView(1)
        }
        ivMosaicThree.setOnClickListener {
            mListener?.onMosaicSelectedClick(2)
            updateSelectedView(2)
        }
        sbMosaicBlur.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
               if (mListener!= null){
                    mListener?.onMosaicSettingClick(progress.toFloat() )
               }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
    }

    private fun updateSelectedView(style: Int){
        ivMosaicOne.isSelected = false
        ivMosaicThree.isSelected = false
        ivMosaicTwo.isSelected = false
        when(style){
            0 -> ivMosaicOne.isSelected = true
            1 -> ivMosaicTwo.isSelected = true
            2 -> ivMosaicThree.isSelected = true
        }
    }
    interface OnMosaicTabClickListener {
        fun onMosaicSelectedClick(tabIndex: Int)
        fun onMosaicSettingClick(progress: Float)
    }
}
