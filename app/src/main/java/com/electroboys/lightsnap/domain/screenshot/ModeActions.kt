package com.electroboys.lightsnap.domain.screenshot

interface ModeActions {
    fun enterAddText()// 进入文字编辑模式
    fun exitAddText()//  退出文字编辑模式
    fun enterGraffiti()//  进入涂鸦模式
    fun exitGraffiti()//  退出涂鸦模式
    fun enterArrow() // 进入箭头模式
    fun exitArrow() // 退出箭头模式
    fun enterMosaic()//  进入马赛克模式
    fun enterBox() //进入框选模式
    fun exitBox()//退出框选模式
    fun exitMosaic()//  退出马赛克模式
    fun toggleCrop()//  切换裁剪开关
    fun onEnterOCR()//  进入文字识别模式
}