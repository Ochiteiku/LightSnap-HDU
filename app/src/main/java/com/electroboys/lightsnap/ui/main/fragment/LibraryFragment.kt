package com.electroboys.lightsnap.ui.main.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.ui.main.adapter.LibraryPictureAdapter
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.edit
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import okhttp3.internal.notify

class LibraryFragment : Fragment(R.layout.fragment_library){
    private lateinit var recyclerImageView: RecyclerView
    private lateinit var recyclerViewAdapter: LibraryPictureAdapter
    private lateinit var photoView: PhotoView

    private lateinit var searchEditText: EditText
    private lateinit var filter: Chip
    private lateinit var startSortMenu: ImageButton
    private lateinit var deleteAll: ImageButton
    private lateinit var sortPicker: Spinner
    private lateinit var infoPanel: LinearLayout

    // 图片库的图片信息
    private var imageUris = mutableListOf<Uri>()
    private var ChangedimageUris = mutableListOf<Uri>()
    private var currentImageUri: Uri? = null
    private var imageInformations = mutableListOf<ImageInformation>()

    // 用户选择的信息
    private var StartDate = MaterialDatePicker.todayInUtcMilliseconds()
    private var EndDate = MaterialDatePicker.todayInUtcMilliseconds()  // 开始与结束日期均默认显示为当天
    // 待搜索的图片名称（模糊匹配）
    private lateinit var SearchImageName: String

    private var isInfoPanelVisible = false

    data class ImageInformation(
        var imageUri: Uri,
        var imageDate: Date,
        var imageSize: Long,
        var imageName: String
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchEditText = view.findViewById(R.id.SearchEditText)
        searchEditText.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
            override fun afterTextChanged(s: Editable?) {
                // 输入完成进行搜索
                SearchImageName = s.toString()
                searchImageByName(SearchImageName)
            }
        })

        filter = view.findViewById(R.id.Filter)
        filter.text = formatDateRange()
        filter.setOnClickListener{
            showDatePickerDialog() // 获取选择的日期
        }

        // 开启下拉选框
        startSortMenu = view.findViewById(R.id.StartSortMenu)
        startSortMenu.setOnClickListener{
            sortPicker.isVisible = true
        }

        deleteAll = view.findViewById(R.id.btnDeleteAll)
        deleteAll.setOnClickListener{
            showDeleteDialog()
        }

        sortPicker = view.findViewById(R.id.SortPicker)
        // 设置排序的适配器(懒得拆分)
        val sortPickerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.librarySortMenus)
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        sortPicker.adapter = sortPickerAdapter
        // 设置Spinner选择监听
        sortPicker.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ){
                // 排序方式
                val sortOrder = (resources.getStringArray(R.array.librarySortMenus))[position]
                if(sortOrder.equals("按日期排序")){
                    sortImagesByDate()
                    sortPicker.isVisible = false
                } else if(sortOrder.equals("按图片大小排序")) {
                    sortImagesByLength()
                    sortPicker.isVisible = false
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        recyclerImageView = view.findViewById(R.id.library_picture)
        // recyclerView中垂直方向上显示3列图片
        recyclerImageView.layoutManager = GridLayoutManager(context, 3)
        recyclerViewAdapter = LibraryPictureAdapter(imageUris).apply {
            // 实现长按监听逻辑
            onImageViewLongClickListener = { position: Int ->
                val uri = imageUris[position]
                val documentFile = DocumentFile.fromSingleUri(requireContext(), uri)
                // 确认删除的弹窗
                AlertDialog.Builder(context)
                    .setTitle("删除截图")
                    .setMessage("确定删除该截图吗？")
                    .setPositiveButton("删除") { _, _ ->
                        if (documentFile != null && documentFile.exists()) {
                            if (documentFile.delete()) {
                                imageUris.removeAt(position)
                                notifyItemRemoved(position)
                                Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "删除失败", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "文件不存在或无访问权限", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("取消", null)
                    .show().apply {
                        getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.GRAY)
                    }
            }
            onImageViewClickListener = { position: Int ->
                currentImageUri = imageUris[position]
                // 设置图片在photoview中显示(?通过Glide载入)
                photoView.setImageURI(currentImageUri)
                photoView.visibility = View.VISIBLE
                recyclerImageView.visibility = View.GONE
                photoView.animate().alpha(1f).setDuration(300).start()
            }
        }
        // 设置recyclerView的adapter
        loadOrRefreshImages(false)
        recyclerImageView.adapter = recyclerViewAdapter
        // 实现下拉刷新监听
        recyclerImageView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // dx表示垂直滚动 dy表示上下滚动
                val gridLayoutManager = recyclerView.layoutManager as GridLayoutManager
                val firstVisibleItemPos = (gridLayoutManager as LinearLayoutManager).findFirstVisibleItemPosition()  // 需要向上转型才能使用findFristItem方法
                if(firstVisibleItemPos == 0 && dy < 0){
                    loadOrRefreshImages(true)
                    recyclerViewAdapter.notifyItemChanged(0) // 刷新头部
                    Toast.makeText(context,"刷新成功",Toast.LENGTH_SHORT).show()
                }
            }
        })

        photoView = view.findViewById(R.id.photoView)
        // photoView点击关闭大图
        photoView.setOnClickListener{
            photoView.animate().alpha(0f).setDuration(300).withEndAction{
                    photoView.visibility = View.GONE
                }.start()
            infoPanel.visibility = View.GONE
            recyclerImageView.visibility = View.VISIBLE
        }
        photoView.setOnSingleFlingListener{
             _, _, _, velocityY ->
            if(velocityY < -2000) {
                // 上滑
                showInfoPanel()
                true
            }else{
                false
            }
        }
        infoPanel = view.findViewById(R.id.infoPanel)

    }
    private fun showInfoPanel(){
        var currentImageInfo = imageInformations.find { it.imageUri == currentImageUri }

        view?.findViewById<TextView>(R.id.textName)?.text = "文件名：${currentImageInfo?.imageName}"
        view?.findViewById<TextView>(R.id.textSize)?.text = "文件大小：${currentImageInfo?.imageSize}"
        view?.findViewById<TextView>(R.id.textDate)?.text = "文件最后修改日期：${currentImageInfo?.imageDate}"
        infoPanel.visibility = View.VISIBLE
        isInfoPanelVisible = true

        infoPanel.postDelayed({ infoPanel.visibility = View.GONE }, 5000)  // 显示5秒自动隐藏
    }
    private fun showDeleteDialog(){
        // 确认删除的弹窗
        AlertDialog.Builder(context)
            .setTitle("删除全部截图")
            .setMessage("确定删除全部截图吗？")
            .setPositiveButton("删除") { _, _ ->
                imageUris.clear()
                imageInformations.clear()
                deleteAllimages()
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
            StartDate = dateRange.first
            EndDate = dateRange.second

            filter.text = formatDateRange()

            // 此时更新视图
            filterImages() // 将图片进行过滤
            updateRecyclerViewData() // 更新视图
        }
        datePickerDialog.show(parentFragmentManager,"DATE_RANGE_PICKER")
    }

    private fun formatDateRange(): String{
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        return "${dateFormat.format(Date(StartDate))}-${dateFormat.format(Date(EndDate))}"
    }

    private fun searchImageByName(fileName: String){
        // 包含了输入字符的图片都显示
        ChangedimageUris = imageInformations
            .filter { it.imageName.contains(fileName) }
            .map { it.imageUri }
            .toMutableList()
        // 更新视图
        updateRecyclerViewData()
    }
    private fun filterImages(){
        // 过滤image
        ChangedimageUris = imageInformations
            .filter { it.imageDate.time in StartDate..EndDate }  // 过滤不在时间范围内的截图
            .map { it.imageUri }
            .toMutableList()
    }
    private fun sortImagesByDate(){
        ChangedimageUris = imageInformations
            .filter { it.imageUri in ChangedimageUris }  // 应排序当前视图（已经经过筛选的）中的截图
            .sortedBy { it.imageDate.time }
            .map { it.imageUri }
            .toMutableList()
        // 排序后立刻更新视图
        updateRecyclerViewData()
    }
    private fun sortImagesByLength(){
        ChangedimageUris = imageInformations
            .filter { it.imageUri in ChangedimageUris }
            .sortedBy { it.imageSize }
            .map { it.imageUri }
            .toMutableList()
        updateRecyclerViewData()
    }

    private fun updateRecyclerViewData() {
        recyclerViewAdapter.updateData(ChangedimageUris) // 调用 Adapter 的更新方法
    }

    private fun deleteAllimages(){
        recyclerViewAdapter.deleteData()
//        val sharedPreferences = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
//        val savedPath = sharedPreferences.getString("screenshot_save_uri", null)
//        DocumentFile.fromTreeUri(requireContext(), savedPath!!.toUri())?.delete()
        // 不安全，先不开放该功能
    }
    private fun loadOrRefreshImages(isRefreshing: Boolean) {
//        imageUris.clear()
//        imageInformations.clear()

        val sharedPreferences = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val savedPath = sharedPreferences.getString("screenshot_save_uri", null)

        val lastLoadedTime = sharedPreferences.getLong("libraryImage_last_loaded_time", 0L)

        if (savedPath == null) {
            Toast.makeText(requireContext(), "未设置自定义文件夹路径", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = savedPath.toUri()

        val imageMimeTypes = listOf("image/png", "image/jpeg", "image/jpg", "image/webp")

        if (uri.scheme == "content") {
            // SAF 模式
            val pickedDir = DocumentFile.fromTreeUri(requireContext(), uri)
            if (pickedDir == null || !pickedDir.isDirectory) {
                Toast.makeText(requireContext(), "文件夹无效或无法访问", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), "文件夹路径无效", Toast.LENGTH_SHORT).show()
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
        ChangedimageUris = imageUris // 初始化用于排序中

        // 记录最后加载的时间，便于刷新数据时不会重复加载
        sharedPreferences.edit() {
            putLong("libraryImage_last_loaded_time", System.currentTimeMillis())
        }

        recyclerImageView.adapter?.notifyDataSetChanged()
    }
}