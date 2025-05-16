package com.electroboys.lightsnap.domain.screenshot

import com.electroboys.lightsnap.data.screenshot.ControlViewStatus

interface ModeActions {
    fun enterAddText()
    fun exitAddText()

    fun enterGraffiti()
    fun exitGraffiti()

    fun enterMosaic()
    fun exitMosaic()

    fun enterArrow()
    fun exitArrow()

    fun toggleCrop()

    fun onEnterOCR()

    fun showControlPanel(mode: ControlViewStatus) // 让 ModeManager 通知 UI 展示控件
}