package com.electroboys.lightsnap.ui.main.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.ContentUris
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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

class LibraryFragment : Fragment(R.layout.fragment_library){
    private lateinit var libraryFragment: RecyclerView
    private val imageUris = mutableListOf<Uri>()
    private val REQUEST_CODE_PERMISSIONS = 101

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

        // recyclerView中垂直方向上显示2列图片
        libraryFragment.layoutManager = GridLayoutManager(context, 3)

        // 检查权限
        checkAndRequestPermissions()

        // 设置adapter
        val adapter = LibraryPictureAdapter(imageUris).apply {
            // 实现长按监听逻辑
            onItemLongClickListener = {
                position: Int ->
                AlertDialog.Builder(context)
                    .setTitle("删除截图")
                    .setMessage("确定删除截图吗？")
                    .setPositiveButton("删除"){
                        _, _ ->
                        imageUris.removeAt(position)
                        notifyItemRemoved(position)
                    }
                    .setNegativeButton("取消", null)
                    .show().apply {
                        getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.GRAY)
                    }
            }
        }

        libraryFragment.adapter = adapter
    }

    private fun checkAndRequestPermissions() {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (requiredPermissions.all {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }) {
            loadImages()
        } else {
            requestPermissions(requiredPermissions, REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadImages()
        } else {
            showPermissionDeniedMessage()
        }
    }

    private fun showPermissionDeniedMessage() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES) ||
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {

            AlertDialog.Builder(requireContext())
                .setTitle("需要权限")
                .setMessage("需要访问照片权限来显示您的图片")
                .setPositiveButton("确定") { _, _ ->
                    checkAndRequestPermissions()
                }
                .setNegativeButton("取消", null)
                .show()
        } else {
            Toast.makeText(
                requireContext(),
                "请在设置中授予权限",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun loadImages() {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        requireContext().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val imageUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                imageUris.add(imageUri)
            }
        }
    }
}