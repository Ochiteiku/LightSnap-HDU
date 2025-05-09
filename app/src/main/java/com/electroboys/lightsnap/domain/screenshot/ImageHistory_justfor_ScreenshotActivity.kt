package com.electroboys.lightsnap.domain.screenshot

import java.util.Stack

object ImageHistory {
    private val undoStack = Stack<String>() // 历史栈
    private val redoStack = Stack<String>() // 重做栈

    // 添加当前 KEY 到 undo 栈，并清空 redo 栈
    fun push(bitmapKey: String) {
        undoStack.push(bitmapKey)
        redoStack.clear()
    }

    // 获取上一个图像 KEY ，并把当前 KEY 添加到 redo 栈
    fun pop(): String? {
        if (undoStack.size < 2) return null
        val current = undoStack.pop()
        redoStack.push(current)
        return undoStack.peek()
    }

    // 重做：从 redo 栈取出最近撤销的 key
    fun redo(): String? {
        if (redoStack.isEmpty()) return null
        val key = redoStack.pop()
        undoStack.push(key)
        return key
    }

    // 清除历史记录
    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }

    // 是否可以撤销
    fun canUndo(): Boolean {
        return undoStack.size >= 2
    }

    // 是否可以重做
    fun canRedo(): Boolean {
        return redoStack.isNotEmpty()
    }
}
