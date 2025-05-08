package com.electroboys.lightsnap.ui.main.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.Toast
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.domain.screenshot.BitmapCache
import com.electroboys.lightsnap.domain.screenshot.ImageHistory
import com.electroboys.lightsnap.domain.screenshot.SelectView
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.IOException
import androidx.core.content.edit
import com.electroboys.lightsnap.utils.ImageSaveUtil
import com.electroboys.lightsnap.utils.PathPickerUtil
import java.io.File
import java.io.FileOutputStream

class ScreenshotActivity : AppCompatActivity() {

    private lateinit var selectView: SelectView
    private lateinit var imageView: ImageView
    private lateinit var btnConfirmSelection: ImageButton
    private lateinit var folderLauncher: ActivityResultLauncher<Intent>


    companion object {
        const val EXTRA_SCREENSHOT_KEY = "screenshot_key"
    }

    private var isDragging = false
    private val startTouch = PointF()
    private val endTouch = PointF()
    private var originalBitmapKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screenshot)

        // 初始化路径选择 launcher
        folderLauncher = PathPickerUtil.pickFolder(this) { treeUri ->
            if (treeUri != null) {
                val currentKey = intent.getStringExtra(EXTRA_SCREENSHOT_KEY)
                val bitmap = currentKey?.let { BitmapCache.getBitmap(it) }

                if (bitmap != null) {
                    val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
                    sharedPreferences.edit() {
                        putString(
                            "screenshot_save_uri",
                            treeUri.toString()
                        )
                    }

                    // 保存图片到指定路径
                    ImageSaveUtil.saveBitmapWithName(
                        context = this,
                        bitmap = bitmap,
                        treeUri = treeUri
                    ) { success ->
                        if (success) {
                            finish()
                        }
                    }
                } else {
                    Toast.makeText(this, "图片数据不可用", Toast.LENGTH_SHORT).show()
                }
            }
        }

        imageView = findViewById(R.id.imageViewScreenshot)
        selectView = findViewById(R.id.selectView)
        btnConfirmSelection = findViewById(R.id.btnConfirmSelection)

        // 撤销键逻辑
        val btnBack = findViewById<ImageButton>(R.id.btnUndo)
        btnBack.setOnClickListener {
            // 执行撤销操作
            undoToLastImage()
        }

        // 保存键逻辑
        val btnSave = findViewById<ImageButton>(R.id.btnSave)
        btnSave.setOnClickListener {
            saveCurrentImage()
        }

        // 获取传入的 key
        val key = intent.getStringExtra(EXTRA_SCREENSHOT_KEY)
        originalBitmapKey = key
        key?.let {
            ImageHistory.push(it)
        }

        // 从缓存中取出 Bitmap
        val bitmap = key?.let { BitmapCache.getBitmap(it) }

//        // 编辑键逻辑 跳转到编辑Activity
//        val btnEditWrapper = findViewById<LinearLayout>(R.id.btnEditWrapper)
//        btnEditWrapper.setOnClickListener{
//            // 通过缓存实现数据传递
//            val tmpFile = File(cacheDir, "tmp_img.jpeg")
//            bitmap?.compress(Bitmap.CompressFormat.JPEG, 90, FileOutputStream(tmpFile))
//
//            val intent = Intent(this, EditScreenshotActivity::class.java).apply {
//                putExtra("image_path", tmpFile.absoluteFile)
//            }
//
//            startActivity(intent)
//        }

        Log.d("ScreenshotExampleActivity", "Test：this  is called")
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
        } else {
            Toast.makeText(this, "截图数据为空或已释放", Toast.LENGTH_SHORT).show()
        }

        Log.d("ScreenshotExampleActivity", "准备调用 setupTouchListener()")

        //设置监听器和交互逻辑
        setupTouchListener()

        //绑定确认选区按钮点击事件
        setupConfirmButtonClickListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清理 Bitmap 缓存
        BitmapCache.clear()
    }

    private fun undoToLastImage() {
        if (!ImageHistory.canUndo()) {
            Toast.makeText(this, "没有更多可撤销的操作", Toast.LENGTH_SHORT).show()
            return
        }

        val previousKey = ImageHistory.pop() ?: run {
            Toast.makeText(this, "无法获取上一步图像", Toast.LENGTH_SHORT).show()
            return
        }

        val previousBitmap = BitmapCache.getBitmap(previousKey) ?: run {
            Toast.makeText(this, "上一步图像已被释放", Toast.LENGTH_SHORT).show()
            return
        }

        imageView.setImageBitmap(previousBitmap)

        // 更新缓存 KEY
        intent.putExtra(EXTRA_SCREENSHOT_KEY, previousKey)

        // 清除选区和 UI 状态
        selectView.clearSelection()
        btnConfirmSelection.visibility = View.GONE
        findViewById<TextView>(R.id.selectionHint).visibility = View.VISIBLE

        Toast.makeText(this, "已恢复至上一步", Toast.LENGTH_SHORT).show()
    }

    private fun setupTouchListener() {
        val imageContainer = findViewById<View>(R.id.imageContainer)

        // 使用 post 确保 View 已完成 layout
        imageContainer.post {
            Log.d("ScreenshotExampleActivity", "setupTouchListener调用成功")
            Log.d("ScreenshotExampleActivity", "imageContainer size after layout: ${imageContainer.width} x ${imageContainer.height}")

            imageContainer.setOnTouchListener { v, event ->
                Log.d("ScreenshotExampleActivity", "触摸事件发生")
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startTouch.set(event.x, event.y)
                        endTouch.set(event.x, event.y)
                        isDragging = true
                        selectView.clearSelection()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (isDragging) {
                            endTouch.set(event.x, event.y)
                            updateAndInvalidateSelection()
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isDragging) {
                            endTouch.set(event.x, event.y)
                            updateAndInvalidateSelection()
                            isDragging = false
                        }
                        v.performClick()
                    }
                }
                true
            }
        }
    }

    private fun updateAndInvalidateSelection() {
        val left = minOf(startTouch.x, endTouch.x).toInt()
        val top = minOf(startTouch.y, endTouch.y).toInt()
        val right = maxOf(startTouch.x, endTouch.x).toInt()
        val bottom = maxOf(startTouch.y, endTouch.y).toInt()

        val selectionRect = Rect(left, top, right, bottom)

        Log.d("ScreenshotExampleActivity", "Drawing Rect: $selectionRect")

        val start = PointF(selectionRect.left.toFloat(), selectionRect.top.toFloat())
        val end = PointF(selectionRect.right.toFloat(), selectionRect.bottom.toFloat())

        if (::selectView.isInitialized) {
            Log.d("ScreenshotExampleActivity", "selectView 已初始化，准备设置选区")
            selectView.setSelection(start, end)
        } else {
            Log.e("ScreenshotExampleActivity", "selectView 未初始化！无法设置选区")
        }

        // 同步更新提示文字可见性
        val hintTextView = findViewById<TextView>(R.id.selectionHint)
        if (selectionRect.isEmpty) {
            hintTextView.visibility = View.VISIBLE
            btnConfirmSelection.visibility = View.GONE
        } else {
            hintTextView.visibility = View.GONE
            btnConfirmSelection.visibility = View.VISIBLE

            // 使用 post 延迟获取 btnConfirmSelection 的宽高
            btnConfirmSelection.post {
                val params = btnConfirmSelection.layoutParams as FrameLayout.LayoutParams
                params.gravity = Gravity.TOP or Gravity.START
                params.leftMargin = selectionRect.right - btnConfirmSelection.width
                params.topMargin = selectionRect.bottom - btnConfirmSelection.height
                btnConfirmSelection.layoutParams = params
            }
        }
    }


    private fun setupConfirmButtonClickListener() {
        btnConfirmSelection.setOnClickListener {
            val bitmap = intent.getStringExtra(EXTRA_SCREENSHOT_KEY)?.let { BitmapCache.getBitmap(it) }
            val selectedRect = getBitmapCropRect(imageView)

            if (bitmap != null && selectedRect != null && !selectedRect.isEmpty) {
                val croppedBitmap = Bitmap.createBitmap(
                    bitmap,
                    selectedRect.left,
                    selectedRect.top,
                    selectedRect.width(),
                    selectedRect.height()
                )
                imageView.setImageBitmap(croppedBitmap)

                // 更新缓存中的 KEY
                val newKey = BitmapCache.cacheBitmap(croppedBitmap)
                intent.putExtra(EXTRA_SCREENSHOT_KEY, newKey)
                ImageHistory.push(newKey)

                // 清除选区
                selectView.clearSelection()
                btnConfirmSelection.visibility = View.GONE
                val hintTextView = findViewById<TextView>(R.id.selectionHint)
                hintTextView.visibility = View.VISIBLE

                Toast.makeText(this, "已裁剪并更新图像", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "无效选区或图片为空", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getBitmapCropRect(imageView: ImageView): Rect? {
        val drawable = imageView.drawable ?: return null
        val bitmapWidth = drawable.intrinsicWidth
        val bitmapHeight = drawable.intrinsicHeight

        val viewWidth = imageView.width
        val viewHeight = imageView.height

        if (bitmapWidth <= 0 || bitmapHeight <= 0 || viewWidth <= 0 || viewHeight <= 0) {
            return null
        }

        val imageMatrix = imageView.imageMatrix
        val values = FloatArray(9)
        imageMatrix.getValues(values)

        val scaleX = values[Matrix.MSCALE_X]
        val scaleY = values[Matrix.MSCALE_Y]
        val transX = values[Matrix.MTRANS_X]
        val transY = values[Matrix.MTRANS_Y]

        // 将触摸坐标转换为图像本地坐标
        val left = ((startTouch.x - transX) / scaleX).toInt()
        val top = ((startTouch.y - transY) / scaleY).toInt()
        val right = ((endTouch.x - transX) / scaleX).toInt()
        val bottom = ((endTouch.y - transY) / scaleY).toInt()

        // 边界检查
        val validLeft = maxOf(0, minOf(left, right))
        val validTop = maxOf(0, minOf(top, bottom))
        val validRight = minOf(bitmapWidth, maxOf(left, right))
        val validBottom = minOf(bitmapHeight, maxOf(top, bottom))

        return Rect(validLeft, validTop, validRight, validBottom)
    }

    private fun saveCurrentImage() {
        val imageView = findViewById<ImageView>(R.id.imageViewScreenshot)
        val bitmap = (imageView.drawable as? BitmapDrawable)?.bitmap
            ?: run {
                Toast.makeText(this, "无法获取图片", Toast.LENGTH_SHORT).show()
                return
            }

//        // Test：清除文件路径缓存
//        getSharedPreferences("settings", Context.MODE_PRIVATE)
//            .edit() {
//                remove("screenshot_save_uri")
//            }

        val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)

        val uriString = sharedPreferences.getString("screenshot_save_uri", null) ?: run {
            showSetPathDialog { confirmed ->
                if (confirmed) {
                    // 启动路径选择器
                    folderLauncher.launch(
                        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                            addFlags(
                                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                            )
                        }
                    )
                } else {
                    Toast.makeText(this, "保存已取消", Toast.LENGTH_SHORT).show()
                }
            }
            return
        }

        val treeUri = uriString.toUri()

        // 保存图片到指定路径
        ImageSaveUtil.saveBitmapWithName(
            context = this,
            bitmap = bitmap,
            treeUri = treeUri
        ) { success ->
            if (success) {
                finish()
            }
        }
    }


    private fun showSetPathDialog(onConfirm: (Boolean) -> Unit) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_confirm_set_path, null)
        dialog.setContentView(view)

        // 设置默认展开高度
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
                behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
            }
        }

        val btnConfirm = view.findViewById<Button>(R.id.btnConfirm)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            onConfirm(true)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
            onConfirm(false)
        }

        dialog.show()
    }

}
