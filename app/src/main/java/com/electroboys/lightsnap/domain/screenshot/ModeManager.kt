package com.electroboys.lightsnap.domain.screenshot

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.electroboys.lightsnap.ui.main.activity.ScreenshotActivity
import com.electroboys.lightsnap.ui.main.activity.ScreenshotActivity.Companion.Mode

class ModeManager(private val actions: ModeActions) {

    private var currentMode: Mode = Mode.None
    fun enter(mode: Mode) {

        // 关闭已有模式
        exitCurrentMode()

        // 进入新模式
        when (mode) {
            Mode.AddText -> actions.enterAddText()
            Mode.Graffiti -> actions.enterGraffiti()
            Mode.Mosaic -> actions.enterMosaic()
            Mode.Crop -> {
                if ((actions as? ScreenshotActivity)?.isSelectionEnabled == false) {
                    actions.toggleCrop()
                }
            }
            Mode.OCR -> actions.onEnterOCR()
            Mode.None -> {}
        }

        currentMode = mode
    }

    // 后续有新添加的功能时往里面添加该功能的退出逻辑
    private fun exitCurrentMode() {
        when (currentMode) {
            Mode.AddText -> actions.exitAddText()
            Mode.Graffiti -> actions.exitGraffiti()
            Mode.Mosaic -> actions.exitMosaic()
            Mode.Crop -> {
                Log.d("ModeManager", "CropExitUsed")
                if ((actions as? ScreenshotActivity)?.isSelectionEnabled == true) {
                    actions.toggleCrop()
                }
            }
            //没有退出逻辑的不需要添加，例如OCR,如果后续有添加退出逻辑可以添加
//            Mode.OCR -> actions.onEnterOCR()
            else -> {}
        }
        currentMode = Mode.None
    }

    fun getCurrentMode(): Mode = currentMode
}
