package com.electroboys.lightsnap.ui.main.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.electroboys.lightsnap.domain.screenshot.repository.OcrRepository
import com.electroboys.lightsnap.ui.main.viewmodel.ScreenshotViewModel

class ScreenshotViewModelFactory(
    private val ocrRepo: OcrRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ScreenshotViewModel(ocrRepo) as T
    }
}