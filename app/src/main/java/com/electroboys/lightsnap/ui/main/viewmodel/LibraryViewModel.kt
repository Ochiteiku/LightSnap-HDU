package com.electroboys.lightsnap.ui.main.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.electroboys.lightsnap.domain.library.LibraryManager

class LibraryViewModel: ViewModel() {
    private val _dialogEvent = MutableLiveData<Event<String>>()
    val dialogEvent: LiveData<Event<String>> = _dialogEvent
    fun notifyDialogConfirmed(message: String) {
        _dialogEvent.value = Event(message) // Event 类避免重复消费
    }

    private val _ImageUrisdata = MutableLiveData<MutableList<Uri>>()
    // 对外暴露不可变的 LiveData（Fragment中只能观察，不能修改）
    val ImageUrisdata: LiveData<MutableList<Uri>> = _ImageUrisdata
    // 更新数据的方法
    fun updateImageUriData(newData: MutableList<Uri>) {
        _ImageUrisdata.value = newData
    }

    private val _ImageInfosdate = MutableLiveData<MutableList<LibraryManager.ImageInformation>>()
    val ImageInfodate: LiveData<MutableList<LibraryManager.ImageInformation>> = _ImageInfosdate
    fun updateImageInfoData(newData: MutableList<LibraryManager.ImageInformation>){
        _ImageInfosdate.value = newData
    }

}

// Event 包装类（防止 LiveData 重复触发）
class Event<T>(private val content: T) {
    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) null else {
            hasBeenHandled = true
            content
        }
    }
}