package com.electroboys.lightsnap.domain.screenshot

import com.electroboys.lightsnap.data.screenshot.ControlViewStatus
import com.electroboys.lightsnap.ui.main.activity.ScreenshotActivity.Companion.Mode

interface ModeActions {

    // 添加文字模式
    fun enterAddText()
    fun exitAddText()

    // 涂鸦模式
    fun enterGraffiti()
    fun exitGraffiti()

    // 马赛克模式
    fun enterMosaic()
    fun exitMosaic()

    // 箭头模式
    fun enterArrow()
    fun exitArrow()

    // 裁剪模式
    fun enterCrop()
    fun exitCrop()

    // 框选模式
    fun enterBox()
    fun exitBox()

    // OCR 模式（进入后可能立即退出，无需 exit）
    fun onEnterOCR()

    // 控制 UI 面板切换（由 ControlPanelManager 实现）
    fun showControlPanel(mode: ControlViewStatus, activeMode: Mode)
}
