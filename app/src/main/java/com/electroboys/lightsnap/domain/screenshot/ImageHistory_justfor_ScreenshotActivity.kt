package com.electroboys.lightsnap.domain.screenshot

import java.util.Stack

object ImageHistory {
    private val historyStack = Stack<String>()

    // 添加当前 KEY 到历史栈
    fun push(bitmapKey: String) {
        historyStack.push(bitmapKey)
    }

    // 获取上一个图像 KEY
    fun pop(): String? {
        return if (historyStack.size > 1) {
            historyStack.pop()
            historyStack.peek()
        } else {
            null
        }
    }

    // 清除历史记录
    fun clear() {
        historyStack.clear()
    }

    // 是否可以撤销
    fun canUndo(): Boolean {
        return historyStack.size >= 2
    }
}
