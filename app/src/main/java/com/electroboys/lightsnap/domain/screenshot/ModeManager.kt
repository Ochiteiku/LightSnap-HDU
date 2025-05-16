package com.electroboys.lightsnap.domain.screenshot

import com.electroboys.lightsnap.data.screenshot.ControlViewStatus
import com.electroboys.lightsnap.ui.main.activity.ScreenshotActivity
import com.electroboys.lightsnap.ui.main.activity.ScreenshotActivity.Companion.Mode

class ModeManager(private val actions: ModeActions) {

    private var currentMode: Mode = Mode.None

    fun enter(mode: Mode) {
        // 退出上一个模式
        exitCurrentMode()

        // 进入新模式并展示控件
        when (mode) {
            Mode.AddText -> actions.enterAddText()
            Mode.Graffiti -> {
                actions.enterGraffiti()
                actions.showControlPanel(ControlViewStatus.GraffitiMode)
            }
            Mode.Arrow -> {
                actions.enterArrow()
                actions.showControlPanel(ControlViewStatus.ArrowMode)
            }
            Mode.Mosaic -> {
                actions.enterMosaic()
                actions.showControlPanel(ControlViewStatus.MosaicMode)
            }
            Mode.Crop -> {
                if ((actions as? ScreenshotActivity)?.isSelectionEnabled == false) {
                    actions.toggleCrop()
                }
                // Crop 不展示 ControlPanel，不调用
            }
            Mode.Framing -> {
                actions.showControlPanel(ControlViewStatus.FramingMode)
            }
            Mode.OCR -> actions.onEnterOCR()
            Mode.None -> actions.showControlPanel(ControlViewStatus.OtherMode)
        }

        currentMode = mode
    }

    private fun exitCurrentMode() {
        when (currentMode) {
            Mode.AddText -> actions.exitAddText()
            Mode.Graffiti -> actions.exitGraffiti()
            Mode.Arrow -> actions.exitArrow()
            Mode.Mosaic -> actions.exitMosaic()
            Mode.Crop -> {
                if ((actions as? ScreenshotActivity)?.isSelectionEnabled == true) {
                    actions.toggleCrop()
                }
            }
            else -> {}
        }
        currentMode = Mode.None
    }

    fun getCurrentMode(): Mode = currentMode
}
