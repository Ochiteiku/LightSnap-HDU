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
import android.widget.FrameLayout
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
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.electroboys.lightsnap.domain.library.LibraryManager
import com.electroboys.lightsnap.ui.main.viewmodel.LibraryViewModel

class LibraryFragment : Fragment(R.layout.fragment_library){
    private lateinit var recyclerImageView: RecyclerView
    private lateinit var recyclerViewAdapter: LibraryPictureAdapter
    private lateinit var infoPanel: LinearLayout
    private lateinit var photoView: PhotoView

    private lateinit var toolbarContainer: FrameLayout
    private lateinit var libraryManager: LibraryManager
    private val viewModel: LibraryViewModel by activityViewModels()

    // 图片库的图片信息
    private var imageUris = mutableListOf<Uri>()
    private var currentImageUri: Uri? = null
    private var currentimageInformations = mutableListOf<LibraryManager.ImageInformation>()

    private var isInfoPanelVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbarContainer = view.findViewById(R.id.libraryTooBarContainer)
        libraryManager = LibraryManager(requireContext(),parentFragmentManager,viewModel,toolbarContainer)
        libraryManager.loadOrRefreshImages(false)
        imageUris = libraryManager.initImagesUri()

        // 监听对话框事件
        viewModel.dialogEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                if(message == "删除所有数据"){
                    deleteAllimages()
                }
            }
        }
        viewModel.ImageUrisdata.observe(viewLifecycleOwner) { newImageUris ->
            // 数据更新时执行操作
            updateRecyclerViewData(newImageUris)
        }

        viewModel.ImageInfodate.observe(viewLifecycleOwner){ newImageInfos ->
            currentimageInformations = newImageInfos
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
        recyclerImageView.adapter = recyclerViewAdapter
        // 实现下拉刷新监听
        recyclerImageView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItemPos = layoutManager.findFirstVisibleItemPosition()
                if (firstVisibleItemPos == 0 && dy < 0) {
                    libraryManager.loadOrRefreshImages(true)
                    Toast.makeText(context, "刷新成功", Toast.LENGTH_SHORT).show()
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
        var currentImageInfo = currentimageInformations.find { it.imageUri == currentImageUri }

        view?.findViewById<TextView>(R.id.textName)?.text = "文件名：${currentImageInfo?.imageName}"
        view?.findViewById<TextView>(R.id.textSize)?.text = "文件大小：${currentImageInfo?.imageSize}"
        view?.findViewById<TextView>(R.id.textDate)?.text = "文件最后修改日期：${currentImageInfo?.imageDate}"
        infoPanel.visibility = View.VISIBLE
        isInfoPanelVisible = true

        infoPanel.postDelayed({ infoPanel.visibility = View.GONE }, 3000)  // 显示3秒自动隐藏
    }

    private fun updateRecyclerViewData(newImageUris: MutableList<Uri>) {
        recyclerViewAdapter.updateData(newImageUris) // 调用 Adapter 的更新方法
    }

    private fun deleteAllimages(){
        recyclerViewAdapter.deleteData()
//        val sharedPreferences = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
//        val savedPath = sharedPreferences.getString("screenshot_save_uri", null)
//        DocumentFile.fromTreeUri(requireContext(), savedPath!!.toUri())?.delete()
        // 不安全，先不开放该功能
    }
}