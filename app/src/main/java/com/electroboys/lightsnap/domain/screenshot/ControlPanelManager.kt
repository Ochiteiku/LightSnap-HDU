package com.electroboys.lightsnap.domain.screenshot

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.electroboys.lightsnap.data.screenshot.ControlViewStatus
import com.electroboys.lightsnap.domain.screenshot.modehandler.ArrowModeHandler
import com.electroboys.lightsnap.domain.screenshot.modehandler.FramingModeHandler
import com.electroboys.lightsnap.domain.screenshot.modehandler.GraffitiModeHandler
import com.electroboys.lightsnap.domain.screenshot.modehandler.MosaicModeHandler
import com.electroboys.lightsnap.ui.main.view.FrameSelectView
import com.electroboys.lightsnap.ui.main.view.GraffitiView

class ControlPanelManager(
    private val context: Context,
    private val imageView: ImageView,
    private val graffitiView: GraffitiView,
    private val frameSelectView: FrameSelectView,
    private val exControlFrame: ViewGroup
) {
    private val modeHandlers = mapOf(
        ControlViewStatus.GraffitiMode to GraffitiModeHandler(),
        ControlViewStatus.MosaicMode to MosaicModeHandler(),
        ControlViewStatus.ArrowMode to ArrowModeHandler(),
        ControlViewStatus.FramingMode to FramingModeHandler()
    )

    fun applyMode(mode: ControlViewStatus) {
        val bitmap = (imageView.drawable as? BitmapDrawable)?.bitmap ?: return

        if (mode == ControlViewStatus.OtherMode) {
            graffitiView.visibility = View.GONE
            frameSelectView.visibility = View.GONE
            exControlFrame.removeAllViews()
            return
        }

        modeHandlers[mode]?.apply(
            context, bitmap, graffitiView, frameSelectView, exControlFrame
        )
    }
}