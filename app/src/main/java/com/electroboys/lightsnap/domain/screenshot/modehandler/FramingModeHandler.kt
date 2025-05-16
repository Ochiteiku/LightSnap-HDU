package com.electroboys.lightsnap.domain.screenshot.modehandler

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import com.electroboys.lightsnap.domain.screenshot.ControlModeHandler
import com.electroboys.lightsnap.ui.main.view.FrameSelectTabView
import com.electroboys.lightsnap.ui.main.view.FrameSelectView
import com.electroboys.lightsnap.ui.main.view.GraffitiView

class FramingModeHandler : ControlModeHandler {
    override fun apply(
        context: Context,
        bitmap: Bitmap,
        graffitiView: GraffitiView,
        frameSelectView: FrameSelectView,
        exControlFrame: ViewGroup
    ) {
        frameSelectView.setBitmap(bitmap)
        frameSelectView.visibility = View.VISIBLE
        graffitiView.visibility = View.GONE

        val tabView = FrameSelectTabView(context)
        exControlFrame.removeAllViews()
        exControlFrame.addView(tabView)

        tabView.setOnSelectedListener(object : FrameSelectTabView.OnSelectedListener {
            override fun onColorSelected(color: Int) {
                frameSelectView.setLineColor(color)
            }

            override fun onSelectSize(size: Int) {
                frameSelectView.setLineWidth(size)
            }

            override fun OnSelectedStyle(style: Int) {
                frameSelectView.setShapeType(style)
            }
        })
    }
}