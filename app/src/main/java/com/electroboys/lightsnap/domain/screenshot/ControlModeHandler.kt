package com.electroboys.lightsnap.domain.screenshot

import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import com.electroboys.lightsnap.ui.main.view.FrameSelectView
import com.electroboys.lightsnap.ui.main.view.GraffitiView

interface ControlModeHandler {
    fun apply(
        context: Context,
        bitmap: Bitmap,
        graffitiView: GraffitiView,
        frameSelectView: FrameSelectView,
        exControlFrame: ViewGroup
    )
}