package com.electroboys.lightsnap.domain.screenshot.modehandler

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import com.electroboys.lightsnap.domain.screenshot.ControlModeHandler
import com.electroboys.lightsnap.ui.main.view.ArrowTabView
import com.electroboys.lightsnap.ui.main.view.FrameSelectView
import com.electroboys.lightsnap.ui.main.view.GraffitiView

class ArrowModeHandler : ControlModeHandler {
    override fun apply(
        context: Context,
        bitmap: Bitmap,
        graffitiView: GraffitiView,
        frameSelectView: FrameSelectView,
        exControlFrame: ViewGroup
    ) {
        graffitiView.setBitmap(bitmap)
        graffitiView.setDrawMode(GraffitiView.DrawMode.ARROW)
        graffitiView.setMosaicMode(false)
        graffitiView.visibility = View.VISIBLE
        frameSelectView.visibility = View.GONE

        val tabView = ArrowTabView(context)
        exControlFrame.removeAllViews()
        exControlFrame.addView(tabView)

        tabView.setOnArrowStyleSelectedListener(object : ArrowTabView.OnArrowStyleSelectedListener {
            override fun onColorSelected(color: Int) {
                graffitiView.setArrowColor(color)
            }

            override fun onWidthSelected(width: Float) {
                graffitiView.setArrowWidth(width)
            }

            override fun onStyleSelected(style: Int) {
                graffitiView.setArrowStyle(style)
            }
        })
    }
}