package com.electroboys.lightsnap.domain.screenshot.watermark

class WatermarkConfig private constructor(
    var text: String,
    var color: Int,
    var alpha: Int,
    var textSize: Float,
    var horizontalSpacing: Int,
    var verticalSpacing: Int,
    var rotation: Float
) {

    companion object {
        // 默认配置
        fun default(): WatermarkConfig {
            return WatermarkConfig(
                text = "ElectroBoys",
                color = 0xFF000000.toInt(), // 黑色
                alpha = 100,                // 半透明
                textSize = 28f,
                horizontalSpacing = 300,
                verticalSpacing = 300,
                rotation = 45f
            )
        }
    }

    // 修改文字内容
    fun setText(text: String): WatermarkConfig {
        this.text = text
        return this
    }

    // 修改颜色
    fun setColor(color: Int): WatermarkConfig {
        this.color = color
        return this
    }

    // 修改不透明度 (0-255)
    fun setAlpha(alpha: Int): WatermarkConfig {
        this.alpha = alpha.coerceIn(0..255)
        return this
    }

    // 修改字体大小
    fun setTextSize(textSize: Float): WatermarkConfig {
        this.textSize = textSize
        return this
    }

    // 修改横向间距
    fun setHorizontalSpacing(spacing: Int): WatermarkConfig {
        this.horizontalSpacing = spacing
        return this
    }

    // 修改纵向间距
    fun setVerticalSpacing(spacing: Int): WatermarkConfig {
        this.verticalSpacing = spacing
        return this
    }

    // 修改旋转角度
    fun setRotation(angle: Float): WatermarkConfig {
        this.rotation = angle
        return this
    }
}
