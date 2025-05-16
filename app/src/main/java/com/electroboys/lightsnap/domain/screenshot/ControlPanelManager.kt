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
    private val exControlFrame: FrameLayout,
    private val intent: Intent,
    private val container: ViewGroup,
    private val btnText: ImageButton
) {
    private val addTextHandler = AddTextModeHandler(context, intent, imageView, exControlFrame, btnText, container)

    private val modeHandlers = mapOf(
        ControlViewStatus.GraffitiMode to GraffitiModeHandler(),
        ControlViewStatus.MosaicMode to MosaicModeHandler(),
        ControlViewStatus.ArrowMode to ArrowModeHandler(),
        ControlViewStatus.FramingMode to FramingModeHandler(),
        ControlViewStatus.AddTextMode to addTextHandler
    )

    fun applyMode(mode: ControlViewStatus) {
        val bitmap = (imageView.drawable as? BitmapDrawable)?.bitmap ?: return

        // 隐藏控件
        graffitiView.visibility = View.GONE
        frameSelectView.visibility = View.GONE
        exControlFrame.removeAllViews()

        modeHandlers[mode]?.apply(context, bitmap, graffitiView, frameSelectView, exControlFrame)
    }

    fun exitAddTextMode() {
        addTextHandler.exit()
    }
}
