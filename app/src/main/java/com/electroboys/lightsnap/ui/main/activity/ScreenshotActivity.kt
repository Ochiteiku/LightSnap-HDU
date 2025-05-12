package com.electroboys.lightsnap.ui.main.activity

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.Toast
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.domain.screenshot.BitmapCache
import com.electroboys.lightsnap.domain.screenshot.ImageHistory
import com.electroboys.lightsnap.domain.screenshot.SelectView
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.core.content.edit
import com.electroboys.lightsnap.domain.watermark.WatermarkConfig
import com.electroboys.lightsnap.domain.watermark.WatermarkOverlayView
import com.electroboys.lightsnap.domain.screenshot.EditScreenshot
import com.electroboys.lightsnap.utils.ImageSaveUtil
import com.electroboys.lightsnap.utils.PathPickerUtil
import com.electroboys.lightsnap.utils.WatermarkUtil
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import androidx.core.view.isGone

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import android.widget.LinearLayout
import android.content.ClipDescription
import android.os.ParcelFileDescriptor
import android.widget.ScrollView
import com.google.mlkit.vision.text.Text
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.LifecycleOwner
import com.electroboys.lightsnap.ui.main.view.GraffitiTabView
import com.electroboys.lightsnap.ui.main.view.GraffitiView
import com.electroboys.lightsnap.ui.main.view.MosaicTabView
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt


class ScreenshotActivity : AppCompatActivity() {

    private lateinit var graffitiView: GraffitiView
    private lateinit var exControlFrame: FrameLayout// 扩展控制面板
    private lateinit var selectView: SelectView
    private lateinit var imageView: ImageView
    private lateinit var btnConfirmSelection: ImageButton
    private lateinit var folderLauncher: ActivityResultLauncher<Intent>

    private lateinit var bitmapEdit: EditScreenshot

    private lateinit var watermarkOverlay: WatermarkOverlayView

    companion object {
        const val EXTRA_SCREENSHOT_KEY = "screenshot_key"
    }

    private var isDragging = false
    private val startTouch = PointF()
    private val endTouch = PointF()
    private var originalBitmapKey: String? = null
    private var isSelectionEnabled = true //框选是否启用,默认开启
    private val watermarkConfig = WatermarkConfig.default() //水印配置
    private var isWatermarkVisible = false // 水印是否显示

    private var textViews: MutableList<TextView> = mutableListOf()
    private val selectedTexts = mutableListOf<String>()
    private lateinit var copyButton: Button
    private var recognizedText: String? = null
    private lateinit var overlayView: ImageView
    private lateinit var croppedBitmap: Bitmap
    private lateinit var buttonLayout: LinearLayout
    private var startX = 0f
    private var startY = 0f

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
        watermarkOverlay = findViewById(R.id.watermarkOverlay)
        graffitiView = findViewById(R.id.graffitiView)
        exControlFrame = findViewById(R.id.exControlFrame)
        overlayView = imageView

        // 文字识别键逻辑
        val btnOcr = findViewById<ImageButton>(R.id.btnOcr)
        btnOcr.setOnClickListener {
            // 执行文字识别操作
            performTextRecognition(croppedBitmap)
        }

        // 摘要键逻辑
        val btnSummary = findViewById<ImageButton>(R.id.btnSummary)
        btnSummary.setOnClickListener {
            // 执行摘要操作
//            if (!recognizedText.isNullOrBlank()) {
//                val fullText = recognizedText!!
//
//                lifecycleOwner.lifecycleScope.launch {
//                    val summary = try {
//                        getSummaryFromSuanliAPI(fullText)
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                        println("捕获到异常：${e.message}")
//                        "请求摘要失败，请检查网络或API Key"
//                    }
//                    showSummaryDialog(summary)
//                }
//            } else {
//                Toast.makeText(this, "请先进行文字识别", Toast.LENGTH_SHORT).show()
//            }
        }

        // 编辑的叠加管理器
        bitmapEdit = EditScreenshot(this,findViewById(R.id.imageContainer))

        // 控制器逻辑
        bitmapEdit.addText(
            btnText = findViewById(R.id.btnText),
            btnColorPicker = findViewById(R.id.btnColor),
            btnConfirmText = findViewById(R.id.btnConfirmText),
            btnCancelText = findViewById(R.id.btnCancelText),
            btnIsBold = findViewById(R.id.btnIsBold),
            textSizeSeekBar = findViewById(R.id.textSizeSeekBar),
            btnAddTextDone = findViewById(R.id.btnAddTextDone),
            textInput = findViewById(R.id.textInput)
        )

        // 撤销键逻辑
        val btnBack = findViewById<ImageButton>(R.id.btnUndo)
        btnBack.setOnClickListener {
            showControlView(ControlViewStatus.OtherMode.ordinal)
            // 执行撤销操作
            undoToLastImage()
        }

        // 重做键逻辑
        val btnRedo = findViewById<ImageButton>(R.id.btnRedo)
        btnRedo.setOnClickListener {
            showControlView(ControlViewStatus.OtherMode.ordinal)
            redoLastImage()
        }

        // 转发键逻辑
        val btnShare = findViewById<ImageButton>(R.id.btnShare)
        btnShare.setOnClickListener {
            shareCurrentImage()
        }

        // 图片复制键逻辑
        val btnCopy = findViewById<ImageButton>(R.id.btnCopy)
        btnCopy.setOnClickListener {
            // 执行复制图片操作
            copyImageToClipboard()
        }

        // 保存键逻辑
        val btnSave = findViewById<ImageButton>(R.id.btnSave)
        btnSave.setOnClickListener {
            showControlView(ControlViewStatus.OtherMode.ordinal)
            saveCurrentImage()
        }

        // 退出键逻辑
        val btnExit = findViewById<ImageButton>(R.id.btnExit)
        btnExit.setOnClickListener {
            showControlView(ControlViewStatus.OtherMode.ordinal)
            finish()
        }

        //  裁剪开关逻辑
        val btnIfCanSelect = findViewById<ImageButton>(R.id.btnIsCanSelect)
        btnIfCanSelect.setOnClickListener {
            showControlView(ControlViewStatus.OtherMode.ordinal)
            toggleSelectionMode()
        }

        // 水印开关逻辑
        val btnWatermark = findViewById<ImageButton>(R.id.btnWatermark)
        btnWatermark.setOnClickListener {
            toggleWatermarkMode()

            // TODO: 修改水印高亮图标
            // TODO: 水印设置
        }

        showControlView(ControlViewStatus.OtherMode.ordinal)
        // 涂鸦按钮逻辑
        val btnGraffiti = findViewById<ImageButton>(R.id.btnDraw)
        btnGraffiti.setOnClickListener {
            if (isSelectionEnabled) {
                toggleSelectionMode()
            }
            showControlView(ControlViewStatus.GraffitiMode.ordinal)
        }

        // 马赛克按钮逻辑
        val btnMosaic = findViewById<ImageButton>(R.id.btnMosaic)
        btnMosaic.setOnClickListener {
            if (isSelectionEnabled) {
                toggleSelectionMode()
            }
            showControlView(ControlViewStatus.MosaicMode.ordinal)
        }

        graffitiView.setOnBitmapChangeListener(object : GraffitiView.onBitmapChangeListener {
            override fun onBitmapChange(bitmap: Bitmap) {
                val newKey = BitmapCache.cacheBitmap(bitmap)
                intent.putExtra(EXTRA_SCREENSHOT_KEY, newKey)
                ImageHistory.push(newKey)
                imageView.setImageBitmap(bitmap)
            }

        })

        // 获取传入的 key
        val key = intent.getStringExtra(EXTRA_SCREENSHOT_KEY)
        originalBitmapKey = key
        key?.let {
            ImageHistory.push(it)
        }

        // 从缓存中取出 Bitmap
        val bitmap = key?.let { BitmapCache.getBitmap(it) }

        if (bitmap != null) {
            croppedBitmap = bitmap
            imageView.setImageBitmap(bitmap)
            graffitiView.setBitmap(croppedBitmap)
        } else {
            Toast.makeText(this, "截图数据为空或已释放", Toast.LENGTH_SHORT).show()
        }

        //设置监听器和交互逻辑
        setupTouchListener()

        //绑定确认选区按钮点击事件
        setupConfirmButtonClickListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清理 Bitmap 缓存
        val currentKey = intent.getStringExtra(EXTRA_SCREENSHOT_KEY)
        BitmapCache.clearExcept(currentKey)
    }

    override fun finish() {
        super.finish()
        //overridePendingTransition(R.anim.fade_out, R.anim.fade_out)
    }

    // 撤销操作
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

    // 重做操作
    private fun redoLastImage() {
        val redoKey = ImageHistory.redo() ?: run {
            Toast.makeText(this, "没有可重做的操作", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap = BitmapCache.getBitmap(redoKey) ?: run {
            Toast.makeText(this, "图片数据已释放", Toast.LENGTH_SHORT).show()
            return
        }

        imageView.setImageBitmap(bitmap)
        intent.putExtra(EXTRA_SCREENSHOT_KEY, redoKey)

        // 更新 UI 状态
        selectView.clearSelection()
        btnConfirmSelection.visibility = View.GONE
        findViewById<TextView>(R.id.selectionHint).visibility = View.VISIBLE

        Toast.makeText(this, "已恢复至下一步", Toast.LENGTH_SHORT).show()
    }


    //  设置触摸监听器
    private fun setupTouchListener() {
        val imageContainer = findViewById<View>(R.id.imageContainer)

        // 使用 post 确保 View 已完成 layout
        imageContainer.post {
            Log.d("ScreenshotExampleActivity", "setupTouchListener调用成功")
            Log.d("ScreenshotExampleActivity", "imageContainer size after layout: ${imageContainer.width} x ${imageContainer.height}")

            imageContainer.setOnTouchListener { v, event ->
                if (!isSelectionEnabled) return@setOnTouchListener false
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

    // 更新选区并重绘
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


    // 设置确认按钮点击监听器
    private fun setupConfirmButtonClickListener() {
        btnConfirmSelection.setOnClickListener {
            val drawable = imageView.drawable as? BitmapDrawable
            val bitmap = drawable?.bitmap
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
                graffitiView.setBitmap(croppedBitmap)
                Toast.makeText(this, "已裁剪并更新图像", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "无效选区或图片为空", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 获取剪裁的矩形
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

        startX = startTouch.x
        startY = startTouch.y

        // 边界检查
        val validLeft = maxOf(0, minOf(left, right))
        val validTop = maxOf(0, minOf(top, bottom))
        val validRight = minOf(bitmapWidth, maxOf(left, right))
        val validBottom = minOf(bitmapHeight, maxOf(top, bottom))

        return Rect(validLeft, validTop, validRight, validBottom)
    }

    private fun shareCurrentImage(){
        val imageView = findViewById<ImageView>(R.id.imageViewScreenshot)
        val bitmap = (imageView.drawable as? BitmapDrawable)?.bitmap
            ?: run {
                Toast.makeText(this, "无法获取图片", Toast.LENGTH_SHORT).show()
                return
            }

        // 将 Bitmap 保存到缓存文件
        val cacheFile = File(cacheDir, "share_temp.png")
        try {
            FileOutputStream(cacheFile).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
        } catch (e: IOException) {
            Toast.makeText(this, "图片保存失败", Toast.LENGTH_SHORT).show()
            return
        }

        // 通过 FileProvider 获取 Uri（适配 Android 7.0+）
        val contentUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider", // 需在 Manifest 中声明 FileProvider
            cacheFile
        )

        // 构建分享 Intent
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, contentUri)
            type = "image/png"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // 临时授权
        }

        // 显示系统分享弹窗
        startActivity(Intent.createChooser(shareIntent, "分享截图到"))

    }

    //  保存图片
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

//        <-------废弃的老的保存确认弹窗出现逻辑-------->
//        val treeUri = uriString.toUri()
//
//        // 保存图片到指定路径
//        ImageSaveUtil.saveBitmapWithName(
//            context = this,
//            bitmap = bitmap,
//            treeUri = treeUri
//        ) { success ->
//            if (success) {
//                finish()
//            }
//        }
        if(isWatermarkVisible){
            // 将水印实际添加到图片中
            val watermarkedBitmap = WatermarkUtil.addWatermark(
                originalBitmap = bitmap,
                config = watermarkConfig
            )

            // 将更新图片和key
            val newKey = BitmapCache.cacheBitmap(watermarkedBitmap)
//            imageView.setImageBitmap(watermarkedBitmap)
            intent.putExtra(EXTRA_SCREENSHOT_KEY, newKey)
//            ImageHistory.push(newKey)
        }else{
            // 将更新图片和key
            val newKey = BitmapCache.cacheBitmap(bitmap)
            intent.putExtra(EXTRA_SCREENSHOT_KEY, newKey)
        }

        val currentKey = intent.getStringExtra(EXTRA_SCREENSHOT_KEY)
            ?: run {
                Toast.makeText(this, "图片数据不可用", Toast.LENGTH_SHORT).show()
                return
            }

        val resultIntent = Intent().apply {
            putExtra("bitmap_key", currentKey)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }


    // 显示设置路径对话框
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

    // 切换裁剪开关
    private fun toggleSelectionMode() {
        isSelectionEnabled = !isSelectionEnabled

        val btnIfCanSelect = findViewById<ImageButton>(R.id.btnIsCanSelect)
        if (!isSelectionEnabled) {
            // 关闭时清空当前选区和 UI 状态
            selectView.clearSelection()
            btnConfirmSelection.visibility = View.GONE
            findViewById<TextView>(R.id.selectionHint).visibility = View.GONE
            btnIfCanSelect.setImageResource(R.drawable.ic_reselect)
            Toast.makeText(this, "裁剪功能已关闭", Toast.LENGTH_SHORT).show()
        } else {
            btnConfirmSelection.visibility = View.GONE
            findViewById<TextView>(R.id.selectionHint).visibility = View.VISIBLE
            btnIfCanSelect.setImageResource(R.drawable.ic_reselect_on)
            Toast.makeText(this, "裁剪功能已开启", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleWatermarkMode(){
        isWatermarkVisible = !isWatermarkVisible
        val key = intent.getStringExtra(EXTRA_SCREENSHOT_KEY)
        val bitmap = key?.let { BitmapCache.getBitmap(it) }
        if (bitmap != null) {
            val btnWatermark = findViewById<ImageButton>(R.id.btnWatermark)
            if(isWatermarkVisible){
                if(watermarkOverlay.isGone){
                    watermarkOverlay.setWatermark(
                        config = watermarkConfig
                    )
                }
                btnWatermark.setImageResource(R.drawable.ic_watermark_on)
                watermarkOverlay.visibility = View.VISIBLE
                Toast.makeText(this, "已添加水印", Toast.LENGTH_SHORT).show()
            } else {
                btnWatermark.setImageResource(R.drawable.ic_watermark)
                watermarkOverlay.visibility = View.INVISIBLE
                Toast.makeText(this, "已取消添加水印", Toast.LENGTH_SHORT).show()
            }
        }
    }



    // 快捷键触发功能
    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
        if (event.action == android.view.KeyEvent.ACTION_DOWN) {
            val isCtrlPressed = event.isCtrlPressed
            val isShiftPressed = event.isShiftPressed

            when (event.keyCode) {
                android.view.KeyEvent.KEYCODE_C -> {
                    if (isCtrlPressed) {
                        Toast.makeText(this, "Ctrl+C 被触发：执行复制", Toast.LENGTH_SHORT).show()
                        // TODO: 执行复制逻辑
                        return true
                    }
                }
                android.view.KeyEvent.KEYCODE_Z -> {
                    if (isCtrlPressed && isShiftPressed) {
                        // 重做操作已经有一个提示弹窗
//                        Toast.makeText(this, "Ctrl+Shift+Z 被触发：执行重做", Toast.LENGTH_SHORT).show()
                        // 执行重做逻辑
                        redoLastImage()
                        return true
                    } else if (isCtrlPressed) {
                        // 撤销操作已经有一个提示弹窗
//                        Toast.makeText(this, "Ctrl+Z 被触发：执行撤销", Toast.LENGTH_SHORT).show()
                        // 执行撤销逻辑
                        undoToLastImage()
                        return true
                    }
                }
                android.view.KeyEvent.KEYCODE_ESCAPE -> {
                    Toast.makeText(this, "ESC 被触发：退出页面", Toast.LENGTH_SHORT).show()
                    // TODO: 可以加确认退出
                    finish()
                    return true
                }
                android.view.KeyEvent.KEYCODE_ENTER -> {
                    Toast.makeText(this, "ENTER 被触发：保存图像", Toast.LENGTH_SHORT).show()
                    // 调用保存逻辑
                    saveCurrentImage()
                    return true
                }
            }
        }

        return super.dispatchKeyEvent(event)
    }

    // 在截图后进行文字识别
    private fun performTextRecognition(bitmap: Bitmap) {
        recognizeText(bitmap,
            onSuccess = { text ->
                // 将识别到的文字存储到recognizedText中，方便后面进行内容摘要
                recognizedText = text
                // 打印识别到的文字
                println("识别到的文字：$text")
            },
            onSuccessWithBlocks = { visionText: Text ->
                // 显示文字在截图对应位置
                showTextOnScreenshotWithInteraction(visionText)
            },
            onFailure = { error ->
                // 打印错误
                error.printStackTrace()
                println("文字识别失败：${error.message}")
            }
        )
    }

    // 封装好的识别函数
    fun recognizeText(
        bitmap: Bitmap,
        onSuccess: (String) -> Unit,
        onSuccessWithBlocks: (Text) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)

        // 使用中文识别器（同时可以识别英文）
        val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                onSuccess(visionText.text)
                onSuccessWithBlocks(visionText)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    private fun showTextOnScreenshotWithInteraction(visionText: Text) {
        val textBlocks = visionText.textBlocks
        val rootView = overlayView.parent as? ViewGroup ?: return

        // 先清除旧的TextView
        textViews.forEach { rootView.removeView(it) }
        textViews.clear()

        val imageView = overlayView // 截图显示的View
        val imageWidth = imageView.width
        val imageHeight = imageView.height

        val originalWidth = croppedBitmap.width
        val originalHeight = croppedBitmap.height

        val scaleX = imageWidth.toFloat() / originalWidth
        val scaleY = imageHeight.toFloat() / originalHeight

        textBlocks.forEach { textBlock ->
            val text = textBlock.text
            val boundingBox = textBlock.boundingBox ?: return@forEach

            // 获取图片矩阵变换参数
            val matrix = imageView.imageMatrix
            val values = FloatArray(9).also { matrix.getValues(it) }

            // 计算映射后的坐标
            val mappedLeft = (boundingBox.left * values[Matrix.MSCALE_X] + values[Matrix.MTRANS_X])
            val mappedTop = (boundingBox.top * values[Matrix.MSCALE_Y] + values[Matrix.MTRANS_Y])
            val mappedRight = (boundingBox.right * values[Matrix.MSCALE_X] + values[Matrix.MTRANS_X])
            val mappedBottom = (boundingBox.bottom * values[Matrix.MSCALE_Y] + values[Matrix.MTRANS_Y])

            val scaledLeft = max(0f, mappedLeft).toInt()
            val scaledTop = max(0f, mappedTop).toInt()
            val scaledRight = max((scaledLeft + 1).toFloat(), min(mappedRight, imageWidth.toFloat())).toInt()
            val scaledBottom = max((scaledTop + 1).toFloat(), min(mappedBottom, imageHeight.toFloat())).toInt()

            val paddingHorizontal = 4.dpToPx()
            val paddingVertical = 2.dpToPx()

            val blockWidth = scaledRight - scaledLeft
            val blockHeight = scaledBottom - scaledTop

            val extraHeight = (blockHeight * 0.3f).toInt() // 多留30%高度避免裁剪
            val layoutWidth = blockWidth
            val layoutHeight = blockHeight + paddingVertical * 2 + extraHeight

            val textView = TextView(this).apply {
                this.text = text
                setTextColor(Color.BLACK)
                setBackgroundColor(Color.parseColor("#E0F0FF"))
                setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
                gravity = Gravity.CENTER
                tag = "ocr_text"

                // 估算动态字体大小
                val area = layoutWidth * layoutHeight
                val estimatedTextSizePx = sqrt(area.toFloat()) * 0.2f
                val textSizeSp = estimatedTextSizePx / resources.displayMetrics.scaledDensity
                textSize = textSizeSp.coerceIn(12f, 36f)

                minimumWidth = 32.dpToPx()
                minimumHeight = 24.dpToPx()

                setOnClickListener {
                    if (selectedTexts.contains(text)) {
                        selectedTexts.remove(text)
                        setBackgroundColor(Color.parseColor("#E0F0FF"))
                    } else {
                        selectedTexts.add(text)
                        setBackgroundColor(Color.parseColor("#AA66CCFF"))
                    }
                }
            }

            rootView.addView(textView)
            textViews.add(textView)

            textView.layoutParams = FrameLayout.LayoutParams(
                layoutWidth,
                layoutHeight
            ).apply {
                leftMargin = scaledLeft
                topMargin = scaledTop
            }
        }
        addCopyButton()
    }

    private fun setSelectedStyle(textView: TextView) {
        textView.setBackgroundColor(Color.parseColor("#AA66CCFF")) // 高亮选中
    }

    private fun setUnselectedStyle(textView: TextView) {
        textView.setBackgroundColor(Color.parseColor("#E0F0FF")) // 恢复原背景
    }

    private fun addCopyButton() {
        val rootView = overlayView.parent as? ViewGroup ?: return

        copyButton = Button(this).apply {
            text = "复制选中内容"
            setTextColor(Color.BLACK)

            // 设置圆角白色背景
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f.dpToPx() // 设置圆角半径
                setColor(Color.WHITE) // 白色背景
            }

            setOnClickListener {
                copySelectedTextsToClipboard()

                if (::buttonLayout.isInitialized) {
                    val parentView = overlayView.parent as? ViewGroup
                    parentView?.removeView(overlayView)
                    parentView?.removeView(buttonLayout)
                }

                // 移除所有识别出的 TextView
                for (textView in textViews) {
                    textView.parent?.let { parent ->
                        if (parent is ViewGroup) {
                            parent.removeView(textView)
                        }
                    }
                }
                textViews.clear()

                // 移除copyButton本身
                this@apply.parent?.let { parent ->
                    if (parent is ViewGroup) {
                        parent.removeView(this@apply)
                    }
                }
            }
        }

        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            rightMargin = 30
            bottomMargin = 30
        }

        rootView.addView(copyButton, layoutParams)
    }

    // dp转换扩展函数
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
    private fun Float.dpToPx(): Float = this * resources.displayMetrics.density

    private fun copySelectedTextsToClipboard() {
        if (selectedTexts.isEmpty()) {
            Toast.makeText(this, "未选中任何内容", Toast.LENGTH_SHORT).show()
            return
        }

        val combinedText = selectedTexts.joinToString(separator = "\n") // 多行拼接
        val clipboardManager = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Selected OCR Text", combinedText)
        clipboardManager.setPrimaryClip(clipData)

        Toast.makeText(this, "已复制${selectedTexts.size}条内容到剪贴板", Toast.LENGTH_SHORT).show()

        // 可选：复制完成后清空选中状态
        selectedTexts.clear()
    }


    private fun copyImageToClipboard() {
        // 创建一个临时文件来存储图片
        val tempFile = File(this.cacheDir, "temp_image.png")
        try {
            // 将截图保存到临时文件
            val outputStream = FileOutputStream(tempFile)
            croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()

            // 使用 FileProvider 获取内容 URI
            val fileProviderUri = FileProvider.getUriForFile(
                this,
                "${this.packageName}.fileprovider",
                tempFile
            )

            // 授予权限给目标应用
            this.grantUriPermission(
                "*",
                fileProviderUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            // 将图片复制到剪贴板
            val clipData = ClipData.newUri(
                this.contentResolver,
                "image/png",
                fileProviderUri
            )
            val clipboardManager = this.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clipboardManager.setPrimaryClip(clipData)

            Toast.makeText(this, "图片已复制到剪贴板", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "复制失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun getSummaryFromSuanliAPI(content: String): String {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // 正确构建JSON，避免非法字符
        val messageObject = JSONObject()
        messageObject.put("role", "user")
        messageObject.put("content", "请根据文本内容生成文本摘要，只需要简洁地呈现摘要内容，不要包含思考过程：" + content)

        val messagesArray = JSONArray()
        messagesArray.put(messageObject)

        val rootObject = JSONObject()
        rootObject.put("model", "free:QwQ-32B")
        rootObject.put("messages", messagesArray)

        val json = rootObject.toString() // 自动转成合法JSON字符串

        val body = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://api.suanli.cn/v1/chat/completions")
            .addHeader("Authorization", "Bearer sk-W0rpStc95T7JVYVwDYc29IyirjtpPPby6SozFMQr17m8KWeo")
            .post(body)
            .build()

        return withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            println("响应状态码：${response.code}")
            val responseBody = response.body?.string() ?: ""
            if (response.isSuccessful) {
                try {
                    val jsonObject = JSONObject(responseBody)
                    val summary = jsonObject
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                    summary.trim()
                } catch (e: Exception) {
                    e.printStackTrace()
                    println("解析摘要返回出错，原始内容：$responseBody")
                    "解析摘要失败，请检查返回格式"
                }
            } else {
                println("请求失败，状态码：${response.code}, 返回内容：$responseBody")
                "摘要失败: ${response.message} (${response.code})"
            }
        }
    }

    private fun showSummaryDialog(summary: String) {
        // 创建一个 AlertDialog.Builder
        val builder = AlertDialog.Builder(this)
        // 设置对话框的标题
        builder.setTitle("内容摘要")
        // 设置对话框的消息为摘要内容
        builder.setMessage(summary)
        // 添加一个“确定”按钮
        builder.setPositiveButton("确定") { dialog, _ ->
            // 点击确定后关闭对话框
            dialog.dismiss()
        }
        // 创建并显示对话框
        val dialog = builder.create()
        dialog.show()
    }
    /**
     * 显示控制视图
     */
    private fun showControlView(mode: Int) {
        when (mode) {
            ControlViewStatus.GraffitiMode.ordinal -> {
                val currentBitmap = (imageView.drawable as? BitmapDrawable)?.bitmap ?: return
                graffitiView.setBitmap(currentBitmap) // 刷新为当前 imageView 的 bitmap

                graffitiView.visibility = View.VISIBLE
                val graffitiTabView = GraffitiTabView(this)
                exControlFrame.removeAllViews()
                exControlFrame.addView(graffitiTabView)
                graffitiView.setMosaicMode(false)
                graffitiTabView.setOnSelectedListener(listener = object : GraffitiTabView.OnSelectedListener {
                    override fun onColorSelected(color: Int) {
                        graffitiView.setStrokeColor(color)
                    }

                    override fun onSelectSize(size: Int) {
                        graffitiView.setStrokeWidth(size)
                    }

                })
            }
            ControlViewStatus.MosaicMode.ordinal -> {
                val currentBitmap = (imageView.drawable as? BitmapDrawable)?.bitmap ?: return
                graffitiView.setBitmap(currentBitmap) // 强制刷新为当前 imageView 的 bitmap

                graffitiView.visibility = View.VISIBLE
                graffitiView.isClickable
                // 显示涂鸦模式
                val doodleTabView = MosaicTabView(this)
                exControlFrame.removeAllViews()
                exControlFrame.addView(doodleTabView)
                // 显示马赛克模式
                graffitiView.setMosaicMode(true)
                doodleTabView.setOnMosaicTabClickListener(listener = object : MosaicTabView.OnMosaicTabClickListener {
                    override fun onMosaicSelectedClick(tabIndex: Int) {
                        graffitiView.setMosaicRadius(tabIndex)
                    }

                    override fun onMosaicSettingClick(progress: Float) {
                        graffitiView.setMosaicBlur(progress.toInt())
                    }

                    override fun onMosaicStyleSelectedClick(i: Int) {
                        graffitiView.setMosaicStyle(style = i)
                    }

                })
            }
            ControlViewStatus.OtherMode.ordinal -> {
                graffitiView.visibility = View.GONE
                // 显示其他模式
                exControlFrame.removeAllViews()
            }
        }
    }

    enum class ControlViewStatus {
        GraffitiMode,//涂鸦模式
        MosaicMode,//马赛克模式
        OtherMode//其他模式
    }

}
