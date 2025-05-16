package com.electroboys.lightsnap.domain.screenshot.modehandler

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import com.electroboys.lightsnap.domain.screenshot.ControlModeHandler
import com.electroboys.lightsnap.ui.main.view.FrameSelectView
import com.electroboys.lightsnap.ui.main.view.GraffitiView
import com.electroboys.lightsnap.ui.main.view.MosaicTabView

class MosaicModeHandler : ControlModeHandler {
    override fun apply(
        context: Context,
        bitmap: Bitmap,
        graffitiView: GraffitiView,
        frameSelectView: FrameSelectView,
        exControlFrame: ViewGroup
    ) {
        graffitiView.setBitmap(bitmap)
        graffitiView.setDrawMode(GraffitiView.DrawMode.MOSAIC)
        graffitiView.setMosaicMode(true)
        graffitiView.visibility = View.VISIBLE
        frameSelectView.visibility = View.GONE

        val tabView = MosaicTabView(context)
        exControlFrame.removeAllViews()
        exControlFrame.addView(tabView)

        tabView.setOnMosaicTabClickListener(object : MosaicTabView.OnMosaicTabClickListener {
            override fun onMosaicSelectedClick(tabIndex: Int) {
                graffitiView.setMosaicRadius(tabIndex)
            }

            override fun onMosaicSettingClick(progress: Float) {
                graffitiView.setMosaicBlur(progress.toInt())
            }

            override fun onMosaicStyleSelectedClick(style: Int) {
                graffitiView.setMosaicStyle(style)
            }
        })
    }
}