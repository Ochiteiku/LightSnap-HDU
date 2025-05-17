package com.electroboys.lightsnap.domain.screenshot.modehandler

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import com.electroboys.lightsnap.domain.screenshot.ControlModeHandler
import com.electroboys.lightsnap.ui.main.view.FrameSelectView
import com.electroboys.lightsnap.ui.main.view.GraffitiTabView
import com.electroboys.lightsnap.ui.main.view.GraffitiView

class GraffitiModeHandler : ControlModeHandler {
    override fun apply(
        context: Context,
        bitmap: Bitmap,
        graffitiView: GraffitiView,
        frameSelectView: FrameSelectView,
        exControlFrame: ViewGroup
    ) {
        graffitiView.setBitmap(bitmap)
        graffitiView.setDrawMode(GraffitiView.DrawMode.GRAFFITI)
        graffitiView.setMosaicMode(false)
        graffitiView.visibility = View.VISIBLE
        frameSelectView.visibility = View.GONE

        val tabView = GraffitiTabView(context)
        exControlFrame.removeAllViews()
        exControlFrame.addView(tabView)

        tabView.setOnSelectedListener(object : GraffitiTabView.OnSelectedListener {
            override fun onColorSelected(color: Int) {
                graffitiView.setStrokeColor(color)
            }

            override fun onSelectSize(size: Int) {
                graffitiView.setStrokeWidth(size)
            }

            override fun onLineStyleSelected(style: Int) {
                graffitiView.setLineStyle(style)
            }
        })
    }
}