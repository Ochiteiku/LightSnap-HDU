package com.electroboys.lightsnap.domain.screenshot

import com.electroboys.lightsnap.data.screenshot.ControlViewStatus
import com.electroboys.lightsnap.ui.main.activity.ScreenshotActivity.Companion.Mode

class ModeManager(private val actions: ModeActions) {

    private var currentMode: Mode = Mode.None
    fun enter(mode: Mode) {

        // 关闭已有模式
        exitCurrentMode()

        // 否则是进入新模式，先退出原有
        exitCurrentMode()

        when (mode) {
            Mode.AddText -> {
                actions.enterAddText()
                actions.showControlPanel(ControlViewStatus.AddTextMode, Mode.AddText)
            }
            Mode.Graffiti -> {
                actions.enterGraffiti()
                actions.showControlPanel(ControlViewStatus.GraffitiMode, Mode.Graffiti)
            }
            Mode.Arrow -> {
                actions.enterArrow()
                actions.showControlPanel(ControlViewStatus.ArrowMode, Mode.Arrow)
            }
            Mode.Mosaic -> {
                actions.enterMosaic()
                actions.showControlPanel(ControlViewStatus.MosaicMode, Mode.Mosaic)
            }
            Mode.Watermark -> {
                actions.enterWatermark()
                actions.showControlPanel(ControlViewStatus.WatermarkMode, Mode.Watermark)
            }
            Mode.Crop -> {
                actions.enterCrop()
                actions.showControlPanel(ControlViewStatus.CropMode, Mode.Crop)
            }
            Mode.Framing -> {
                actions.showControlPanel(ControlViewStatus.FramingMode, Mode.Framing)
            }
            Mode.OCR -> {
                actions.onEnterOCR()
                currentMode = Mode.OCR // 不退出，视作临时行为
                return
            }
            Mode.None -> {
                actions.showControlPanel(ControlViewStatus.OtherMode, Mode.None)
            }
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
                actions.exitCrop()
            }
            else -> {}
        }
        currentMode = Mode.None
    }

    fun getCurrentMode(): Mode = currentMode
}
