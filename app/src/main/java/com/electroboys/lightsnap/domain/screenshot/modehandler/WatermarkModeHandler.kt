package com.electroboys.lightsnap.domain.screenshot.modehandler

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.view.isGone
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.data.screenshot.BitmapCache
import com.electroboys.lightsnap.domain.screenshot.ControlModeHandler
import com.electroboys.lightsnap.domain.screenshot.watermark.WatermarkConfig
import com.electroboys.lightsnap.ui.main.activity.ScreenshotActivity
import com.electroboys.lightsnap.ui.main.view.FrameSelectView
import com.electroboys.lightsnap.ui.main.view.GraffitiView
import com.electroboys.lightsnap.ui.main.view.WatermarkOverlayView
import com.electroboys.lightsnap.ui.main.view.WatermarkSettingBarView

class WatermarkModeHandler(
    private val context: Context,
    private val intent: Intent,
    private val watermarkOverlay: WatermarkOverlayView,
    private val watermarkConfig: WatermarkConfig,
    private val btnWatermark: ImageButton,
    private val watermarkSettingBar: WatermarkSettingBarView
) : ControlModeHandler {

    private var isWatermarkVisible = false

    override fun apply(
        context: Context,
        bitmap: Bitmap,
        graffitiView: GraffitiView,
        frameSelectView: FrameSelectView,
        exControlFrame: ViewGroup
    ) {
        toggleWatermark()
    }

    private fun toggleWatermark() {
        isWatermarkVisible = !isWatermarkVisible
        val key = intent.getStringExtra(ScreenshotActivity.EXTRA_SCREENSHOT_KEY)
        val bitmap = key?.let { BitmapCache.getBitmap(it) }
        if (bitmap != null) {
            if (isWatermarkVisible) {
                if (watermarkOverlay.isGone) {
                    watermarkOverlay.setWatermark(config = watermarkConfig)
                }
                btnWatermark.setImageResource(R.drawable.ic_watermark_on)
                watermarkOverlay.visibility = View.VISIBLE
                watermarkSettingBar.updateUIState(true)
            } else {
                btnWatermark.setImageResource(R.drawable.ic_watermark)
                watermarkOverlay.visibility = View.INVISIBLE
                watermarkSettingBar.updateUIState(false)
            }
        }
    }

    fun refreshWatermark() {
        if (isWatermarkVisible) {
            watermarkOverlay.setWatermark(watermarkConfig)
        }
    }
}