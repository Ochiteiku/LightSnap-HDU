package com.electroboys.lightsnap.domain.library

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.FragmentManager
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.ui.main.view.LibraryBarView
import com.electroboys.lightsnap.ui.main.viewmodel.LibraryViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.channels.BroadcastChannel
import java.io.File
import java.util.Date

class LibraryManager (
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val viewModel: LibraryViewModel,
    private val container: ViewGroup
) {
    private val libraryBarView = LibraryBarView(context)

    private var imageUris = mutableListOf<Uri>()
    private var ChangedimageUris = mutableListOf<Uri>()
    private var imageInformations = mutableListOf<ImageInformation>()

    // 用户选择的信息
    private var PickedDate = mutableMapOf(
        "StartDate" to MaterialDatePicker.todayInUtcMilliseconds(),
        "EndDate" to MaterialDatePicker.todayInUtcMilliseconds()
    )

    data class ImageInformation(
        var imageUri: Uri,
        var imageDate: Date,
        var imageSize: Long,
        var imageName: String
    )

    init {
        setupLibraryBarViewListener()
        container.addView(libraryBarView)
    }
    private fun setupLibraryBarViewListener(){
        libraryBarView.apply {
            searchEditTextListener = {
                SearchImageName ->
                searchImageByName(SearchImageName)
            }

            filterListener = {
                showDatePickerDialog()
            }

            deleteAllListener = {
                showDeleteDialog()
            }

            sortPickerListener = {
                position ->
                // 排序方式
                val sortOrder = (resources.getStringArray(R.array.librarySortMenus))[position]
                if(sortOrder.equals("按日期排序")){
                    sortImagesByDate()
                } else if(sortOrder.equals("按图片大小排序")) {
                    sortImagesByLength()
                }
            }
        }
    }

    private fun sortImagesByDate(){
        ChangedimageUris = imageInformations
            .filter { it.imageUri in ChangedimageUris }  // 应排序当前视图（已经经过筛选的）中的截图
            .sortedBy { it.imageDate.time }
            .map { it.imageUri }
            .toMutableList()
        // 排序后立刻更新视图
        viewModel.updateImageUriData(ChangedimageUris)
    }
    private fun sortImagesByLength(){
        ChangedimageUris = imageInformations
            .filter { it.imageUri in ChangedimageUris }
            .sortedBy { it.imageSize }
            .map { it.imageUri }
            .toMutableList()
        viewModel.updateImageUriData(ChangedimageUris)
    }

    private fun showDeleteDialog(){
        // 确认删除的弹窗
        AlertDialog.Builder(context)
            .setTitle("删除全部截图")
            .setMessage("确定删除全部截图吗？")
            .setPositiveButton("删除") { _, _ ->
                imageUris.clear()
                imageInformations.clear()
                // TODO
                viewModel.notifyDialogConfirmed("删除所有数据")
                Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show().apply {
                getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.GRAY)
            }
    }

    private fun showDatePickerDialog(){
        // 设置可选日期的约束条件
        val constraintsBuilder = CalendarConstraints.Builder().apply {
            setEnd(MaterialDatePicker.todayInUtcMilliseconds()) //最晚时间为今天
        }

        // 创建日期选择的对话窗
        val datePickerDialog = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("选择日期范围")
            //.setCalendarConstraints(constraintsBuilder.build())
            .setTheme(R.style.DatePickerTheme)
            .build()
        datePickerDialog.addOnPositiveButtonClickListener {
                dateRange ->
            PickedDate["StartDate"] = dateRange.first
            PickedDate["EndDate"] = dateRange.second
            // 此时更新视图
            updateBarDateUI()
            filterImages() // 将图片进行过滤
        }
        datePickerDialog.show(fragmentManager,"DATE_RANGE_PICKER")
    }
    private fun updateBarDateUI(){
        libraryBarView.updateDateText(PickedDate)
    }
    private fun filterImages(){
        // 过滤image
        ChangedimageUris = imageInformations
            .filter { it.imageDate.time in PickedDate["StartDate"]!!..PickedDate["EndDate"]!!}  // 过滤不在时间范围内的截图
            .map { it.imageUri }
            .toMutableList()
        viewModel.updateImageUriData(ChangedimageUris)
    }

    private fun searchImageByName(fileName: String){
        // 包含了输入字符的图片都显示
        ChangedimageUris = imageInformations
            .filter { it.imageName.contains(fileName) }
            .map { it.imageUri }
            .toMutableList()
        // 更新视图
        viewModel.updateImageUriData(ChangedimageUris)
    }

    fun loadOrRefreshImages(isRefreshing: Boolean) {
        val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val savedPath = sharedPreferences.getString("screenshot_save_uri", null)

        val lastLoadedTime = sharedPreferences.getLong("libraryImage_last_loaded_time", 0L)

        if (savedPath == null) {
            Toast.makeText(context, "未设置自定义文件夹路径", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = savedPath.toUri()

        val imageMimeTypes = listOf("image/png", "image/jpeg", "image/jpg", "image/webp")

        if (uri.scheme == "content") {
            // SAF 模式
            val pickedDir = DocumentFile.fromTreeUri(context, uri)
            if (pickedDir == null || !pickedDir.isDirectory) {
                Toast.makeText(context, "文件夹无效或无法访问", Toast.LENGTH_SHORT).show()
                return
            }
            var currentFiles = pickedDir.listFiles().toList()
            if(isRefreshing){
                // 如果处于更新状态才进行过滤(提升性能)
                currentFiles = pickedDir.listFiles().filter {
                        file ->
                    file.lastModified() >= lastLoadedTime
                }
            }
            for (file in currentFiles) {
                if (file.isFile && file.type in imageMimeTypes) {
                    imageUris.add(file.uri)
                    imageInformations.add(
                        ImageInformation(
                            imageUri = file.uri,
                            imageDate = Date(file.lastModified()),
                            imageSize = file.length(),
                            imageName = file.name ?: "unknown"
                        )
                    )
                }
            }
        } else {
            // 普通路径模式
            // TODO 使用 FileObserver 监听文件夹变化（避免轮询）
            val dirFile = File(savedPath)
            if (!dirFile.exists() || !dirFile.isDirectory) {
                Toast.makeText(context, "文件夹路径无效", Toast.LENGTH_SHORT).show()
                return
            }
            var currentFiles = dirFile.listFiles()
            if(isRefreshing){
                currentFiles = dirFile.listFiles{
                        file ->
                    file.lastModified() > lastLoadedTime
                }
            }
            currentFiles.forEach { file ->
                val mimeType = when {
                    file.name.endsWith(".png", true) -> "image/png"
                    file.name.endsWith(".jpg", true) || file.name.endsWith(".jpeg", true) -> "image/jpeg"
                    file.name.endsWith(".webp", true) -> "image/webp"
                    else -> null
                }

                if (file.isFile && mimeType != null) {
                    val fileUri = file.toUri()
                    imageUris.add(fileUri)
                    imageInformations.add(
                        ImageInformation(
                            imageUri = fileUri,
                            imageDate = Date(file.lastModified()),
                            imageSize = file.length(),
                            imageName = file.name
                        )
                    )
                }
            }
        }
        // ChangedimageUris = imageUris // 初始化用于排序中

        // 记录最后加载的时间，便于刷新数据时不会重复加载
        sharedPreferences.edit() {
            putLong("libraryImage_last_loaded_time", System.currentTimeMillis())
        }

        viewModel.updateImageInfoData(imageInformations)
    }
    fun initImagesUri(): MutableList<Uri>{
        return imageUris
    }
}