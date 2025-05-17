package com.electroboys.lightsnap.ui.main.viewmodel

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.Rect
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.electroboys.lightsnap.data.screenshot.BitmapCache
import com.electroboys.lightsnap.data.screenshot.ImageHistory
import com.electroboys.lightsnap.domain.screenshot.repository.AbstractRepository
import com.electroboys.lightsnap.domain.screenshot.repository.OcrRepository
import com.google.mlkit.vision.text.Text
import kotlinx.coroutines.launch

class ScreenshotViewModel(private val ocrRepository: OcrRepository) : ViewModel() {

    //OCR用
    private val _recognizedText = MutableLiveData<String>()
    val recognizedText: LiveData<String> = _recognizedText

    private val _recognizedBlocks = MutableLiveData<Text>()
    val recognizedBlocks: LiveData<Text> = _recognizedBlocks

    private val _selectedTexts = MutableLiveData<MutableList<String>>(mutableListOf())
    val selectedTexts: LiveData<List<String>> = _selectedTexts as LiveData<List<String>>

    // 用于存储生成的摘要
    private val abstractRepository = AbstractRepository()

    private val _summaryText = MutableLiveData<String>()
    val summaryText: LiveData<String> = _summaryText

    // 摘要生成时的状态标记
    private val _isGeneratingSummary = MutableLiveData<Boolean>()
    val isGeneratingSummary: LiveData<Boolean> = _isGeneratingSummary

    //撤回和重做逻辑用
    private val _currentBitmap = MutableLiveData<Bitmap>()
    val currentBitmap: LiveData<Bitmap> = _currentBitmap

    private val _currentBitmapKey = MutableLiveData<String>()
    val currentBitmapKey: LiveData<String> = _currentBitmapKey

    //二次裁剪逻辑用
    val isSelectionEnabled = MutableLiveData(true)
    val isDragging = MutableLiveData(false)
    val startTouch = MutableLiveData(PointF())
    val endTouch = MutableLiveData(PointF())
    val selectionRect = MutableLiveData<Rect?>()


    //OCR逻辑用
    fun recognize(bitmap: Bitmap) {
        viewModelScope.launch {
            val result = ocrRepository.recognizeText(bitmap)
            result.onSuccess { text ->
                _recognizedText.value = text.text
                _recognizedBlocks.value = text
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun recognizeAndSummarize(bitmap: Bitmap) {
        viewModelScope.launch {
            _isGeneratingSummary.value = true
            try {
                val result = ocrRepository.recognizeText(bitmap)
                result.onSuccess { textResult ->
                    val summary = abstractRepository.getSummary(textResult.text)
                    _summaryText.value = summary
                }.onFailure {
                    it.printStackTrace()
                    _summaryText.value = "OCR识别失败"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _summaryText.value = "摘要生成失败"
            } finally {
                _isGeneratingSummary.value = false
            }
        }
    }

    fun toggleSelectText(text: String) {
        val current = _selectedTexts.value ?: mutableListOf()
        if (current.contains(text)) {
            current.remove(text)
        } else {
            current.add(text)
        }
        _selectedTexts.value = current
    }

    fun clearSelectedTexts() {
        _selectedTexts.value = mutableListOf()
    }

    //撤回和重做逻辑用
    fun undo() {
        if (!ImageHistory.canUndo()) {
            // 可用另一个 LiveData 通知 UI 显示提示
            return
        }

        val prevKey = ImageHistory.pop() ?: return
        val prevBitmap = BitmapCache.getBitmap(prevKey) ?: return

        _currentBitmapKey.value = prevKey
        _currentBitmap.value = prevBitmap
    }

    fun redo() {
        val redoKey = ImageHistory.redo() ?: return
        val redoBitmap = BitmapCache.getBitmap(redoKey) ?: return

        _currentBitmapKey.value = redoKey
        _currentBitmap.value = redoBitmap
    }

    //二次裁剪逻辑用
    fun startDrag(x: Float, y: Float) {
        startTouch.value = PointF(x, y)
        endTouch.value = PointF(x, y)
        isDragging.value = true
        updateSelectionRect()
    }

    fun updateDrag(x: Float, y: Float) {
        endTouch.value = PointF(x, y)
        updateSelectionRect()
    }

    fun endDrag(x: Float, y: Float) {
        endTouch.value = PointF(x, y)
        isDragging.value = false
        updateSelectionRect()
    }

    private fun updateSelectionRect() {
        val start = startTouch.value ?: return
        val end = endTouch.value ?: return
        val rect = Rect(
            minOf(start.x, end.x).toInt(),
            minOf(start.y, end.y).toInt(),
            maxOf(start.x, end.x).toInt(),
            maxOf(start.y, end.y).toInt()
        )
        selectionRect.value = rect
    }

    fun toggleSelectionEnabled() {
        isSelectionEnabled.value = !(isSelectionEnabled.value ?: true)
    }

    fun clearSelectionRect() {
        selectionRect.value = null
    }

    //OCR逻辑用
    fun recognizeAndCallback(bitmap: Bitmap ,callback: OnTextRecognizedListener) {
        viewModelScope.launch {
            val result = ocrRepository.recognizeText(bitmap)
            result.onSuccess { text ->

                callback.onTextRecognized(text)
                Log.d("OCR", "text recognized: $text")
                Log.d("OCR", "text blocks: ${text.text}")

            }.onFailure {
                it.printStackTrace()
                callback.onTextRecognized(null)
            }
        }
    }
    interface OnTextRecognizedListener {
        suspend fun onTextRecognized(text: Text?)

    }
}