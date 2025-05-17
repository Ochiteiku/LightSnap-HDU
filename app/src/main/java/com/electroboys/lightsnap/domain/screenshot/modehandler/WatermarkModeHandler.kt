package com.electroboys.lightsnap.domain.screenshot.modehandler

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
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
        showWatermark()
    }

    private fun showWatermark() {
        isWatermarkVisible = true

        val key = intent.getStringExtra(ScreenshotActivity.EXTRA_SCREENSHOT_KEY)
        val bitmap = key?.let { BitmapCache.getBitmap(it) } ?: return

        watermarkOverlay.setWatermark(config = watermarkConfig)
        watermarkOverlay.visibility = View.VISIBLE
        watermarkSettingBar.visibility = View.VISIBLE
        watermarkSettingBar.updateUIState(true)
        btnWatermark.setImageResource(R.drawable.ic_watermark_on)
    }

    fun exit() {
        isWatermarkVisible = false
        watermarkOverlay.visibility = View.INVISIBLE
        watermarkSettingBar.visibility = View.GONE
        watermarkSettingBar.updateUIState(false)
        btnWatermark.setImageResource(R.drawable.ic_watermark)
    }

    fun refreshWatermark() {
        if (isWatermarkVisible) {
            watermarkOverlay.setWatermark(watermarkConfig)
        }
    }
}