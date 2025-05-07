package com.electroboys.lightsnap.ui.main.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.ui.main.adapter.LibraryPictureAdapter
import androidx.documentfile.provider.DocumentFile
import androidx.core.net.toUri
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.github.chrisbanes.photoview.PhotoView

class LibraryFragment : Fragment(R.layout.fragment_library){
    private lateinit var libraryFragment: RecyclerView
    private lateinit var photoView: PhotoView
    private val imageUris = mutableListOf<Uri>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        libraryFragment = view.findViewById(R.id.library_picture)
        photoView = view.findViewById<PhotoView>(R.id.photoView)
        
        // 初始状态下隐藏
        photoView.visibility = View.GONE
        // photoView点击关闭大图
        photoView.setOnClickListener{
            photoView.animate().alpha(0f).setDuration(300).withEndAction{
                    photoView.visibility = View.GONE
                }.start()
        }
        
        // recyclerView中垂直方向上显示3列图片
        libraryFragment.layoutManager = GridLayoutManager(context, 3)

        // 设置adapter
        val adapter = LibraryPictureAdapter(imageUris).apply {
            // 实现长按监听逻辑
            onImageViewLongClickListener = { position: Int ->
                val uri = imageUris[position]
                val documentFile = DocumentFile.fromSingleUri(requireContext(), uri)

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
                val uri = imageUris[position]

                // 设置图片在photoview中显示
                photoView.setImageURI(uri)
                photoView.visibility = view.visibility
                photoView.animate().alpha(1f).setDuration(300).start()
            }
        }

        loadImages()
        libraryFragment.adapter = adapter
    }

    private fun loadImages() {
        imageUris.clear()

        val sharedPreferences = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val savedUriStr = sharedPreferences.getString("screenshot_save_uri", null)

        if (savedUriStr == null) {
            Toast.makeText(requireContext(), "未设置自定义文件夹路径", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = savedUriStr.toUri()
        val pickedDir = DocumentFile.fromTreeUri(requireContext(), uri)

        if (pickedDir == null || !pickedDir.isDirectory) {
            Toast.makeText(requireContext(), "文件夹无效或无法访问", Toast.LENGTH_SHORT).show()
            return
        }

        val imageMimeTypes = listOf("image/png", "image/jpeg", "image/jpg", "image/webp")

        for (file in pickedDir.listFiles()) {
            if (file.isFile && file.type in imageMimeTypes) {
                imageUris.add(file.uri)
            }
        }
        libraryFragment.adapter?.notifyDataSetChanged()
    }
}