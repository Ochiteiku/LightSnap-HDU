package com.electroboys.lightsnap.domain.screenshot

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import com.electroboys.lightsnap.data.screenshot.ControlViewStatus
import com.electroboys.lightsnap.domain.screenshot.modehandler.AddTextModeHandler
import com.electroboys.lightsnap.domain.screenshot.modehandler.ArrowModeHandler
import com.electroboys.lightsnap.domain.screenshot.modehandler.CropModeHandler
import com.electroboys.lightsnap.domain.screenshot.modehandler.FramingModeHandler
import com.electroboys.lightsnap.domain.screenshot.modehandler.GraffitiModeHandler
import com.electroboys.lightsnap.domain.screenshot.modehandler.MosaicModeHandler
import com.electroboys.lightsnap.domain.screenshot.modehandler.WatermarkModeHandler
import com.electroboys.lightsnap.domain.screenshot.repository.ImageCropRepository
import com.electroboys.lightsnap.domain.screenshot.watermark.WatermarkConfig
import com.electroboys.lightsnap.ui.main.view.FrameSelectView
import com.electroboys.lightsnap.ui.main.view.GraffitiView
import com.electroboys.lightsnap.ui.main.view.SelectView
import com.electroboys.lightsnap.ui.main.view.WatermarkOverlayView
import com.electroboys.lightsnap.ui.main.view.WatermarkSettingBarView
import com.electroboys.lightsnap.ui.main.viewmodel.ScreenshotViewModel

class ControlPanelManager(
    private val context: Context,
    private val imageView: ImageView,
    private val graffitiView: GraffitiView,
    private val frameSelectView: FrameSelectView,
    private val exControlFrame: FrameLayout,
    private val intent: Intent,
    private val container: ViewGroup,
    private val btnText: ImageButton,
    private val watermarkOverlay: WatermarkOverlayView,
    private val watermarkConfig: WatermarkConfig,
    private val btnWatermark: ImageButton,
    private val watermarkSettingBar: WatermarkSettingBarView,
    private val cropRepository: ImageCropRepository,
    private val imageContainer: View,
    private val selectView: SelectView,
    private val viewModel: ScreenshotViewModel,
    private val selectionHintView: View
) {
    private val addTextHandler = AddTextModeHandler(context, intent, imageView, exControlFrame, btnText, container)
    private val watermarkHandler = WatermarkModeHandler(context, intent, watermarkOverlay, watermarkConfig, btnWatermark, watermarkSettingBar)
    private val cropHandler = CropModeHandler(context, imageView, imageContainer, selectView, graffitiView, cropRepository, viewModel, selectionHintView)

    private val modeHandlers = mapOf(
        ControlViewStatus.GraffitiMode to GraffitiModeHandler(),
        ControlViewStatus.MosaicMode to MosaicModeHandler(),
        ControlViewStatus.ArrowMode to ArrowModeHandler(),
        ControlViewStatus.FramingMode to FramingModeHandler(),
        ControlViewStatus.AddTextMode to addTextHandler,
        ControlViewStatus.WatermarkMode to watermarkHandler,
        ControlViewStatus.CropMode to cropHandler
    )

    fun applyMode(mode: ControlViewStatus) {
        val bitmap = (imageView.drawable as? BitmapDrawable)?.bitmap ?: return

        graffitiView.visibility = View.GONE
        frameSelectView.visibility = View.GONE
        exControlFrame.removeAllViews()

        modeHandlers[mode]?.apply(context, bitmap, graffitiView, frameSelectView, exControlFrame)
        if (mode == ControlViewStatus.WatermarkMode) {
            exControlFrame.addView(watermarkSettingBar)
        }
    }

    fun exitAddTextMode() {
        addTextHandler.exit()
    }

    fun exitCropMode() {
        (modeHandlers[ControlViewStatus.CropMode] as? CropModeHandler)?.exit()
    }

    fun exitWatermarkMode() {
        (modeHandlers[ControlViewStatus.WatermarkMode] as? WatermarkModeHandler)?.exit()
    }

    fun refreshWatermark() {
        (modeHandlers[ControlViewStatus.WatermarkMode] as? WatermarkModeHandler)?.refreshWatermark()
    }
}