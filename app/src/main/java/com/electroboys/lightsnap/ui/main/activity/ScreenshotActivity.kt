package com.electroboys.lightsnap.ui.main.activity

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isGone
import androidx.lifecycle.ViewModelProvider
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.data.screenshot.BitmapCache
import com.electroboys.lightsnap.data.screenshot.ControlViewStatus
import com.electroboys.lightsnap.data.screenshot.ImageHistory
import com.electroboys.lightsnap.domain.screenshot.EditScreenshot
import com.electroboys.lightsnap.domain.screenshot.repository.ImageCropRepository
import com.electroboys.lightsnap.domain.screenshot.repository.OcrRepository
import com.electroboys.lightsnap.domain.screenshot.repository.ScreenshotViewModelFactory
import com.electroboys.lightsnap.domain.screenshot.watermark.WatermarkConfig
import com.electroboys.lightsnap.ui.main.view.GraffitiTabView
import com.electroboys.lightsnap.ui.main.view.GraffitiView
import com.electroboys.lightsnap.ui.main.view.MosaicTabView
import com.electroboys.lightsnap.ui.main.view.SelectView
import com.electroboys.lightsnap.ui.main.view.WatermarkOverlayView
import com.electroboys.lightsnap.ui.main.viewmodel.ScreenshotViewModel
import com.electroboys.lightsnap.utils.WatermarkUtil
import com.google.mlkit.vision.text.Text
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt


class ScreenshotActivity : AppCompatActivity() {

    private lateinit var graffitiView: GraffitiView
    private lateinit var exControlFrame: FrameLayout// 扩展控制面板
    private lateinit var selectView: SelectView
    private lateinit var imageView: ImageView
    private lateinit var btnConfirmSelection: ImageButton

    private lateinit var bitmapEdit: EditScreenshot

    private lateinit var watermarkOverlay: WatermarkOverlayView

    companion object {
        const val EXTRA_SCREENSHOT_KEY = "screenshot_key"
    }

    private var originalBitmapKey: String? = null
    private var isSelectionEnabled = true //框选是否启用,默认开启
    private val watermarkConfig = WatermarkConfig.default() //水印配置
    private var isWatermarkVisible = false // 水印是否显示

    private var textViews: MutableList<TextView> = mutableListOf()
    private lateinit var copyButton: Button
    private lateinit var overlayView: ImageView
    private lateinit var croppedBitmap: Bitmap

    // 初始化 ViewModel
    private lateinit var viewModel: ScreenshotViewModel
    private val cropRepository = ImageCropRepository()


    // dp转换扩展函数
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
    private fun Float.dpToPx(): Float = this * resources.displayMetrics.density

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screenshot)

        val factory = ScreenshotViewModelFactory(
            OcrRepository(),
        )
        viewModel = ViewModelProvider(this, factory).get(ScreenshotViewModel::class.java)

        imageView = findViewById(R.id.imageViewScreenshot)
        selectView = findViewById(R.id.selectView)
        btnConfirmSelection = findViewById(R.id.btnConfirmSelection)
        watermarkOverlay = findViewById(R.id.watermarkOverlay)
        graffitiView = findViewById(R.id.graffitiView)
        exControlFrame = findViewById(R.id.exControlFrame)
        overlayView = imageView

        // OCR键逻辑
        val btnOcr = findViewById<ImageButton>(R.id.btnOcr)
        btnOcr.setOnClickListener {
            viewModel.recognize(croppedBitmap)
        }
        //OCR
        viewModel.recognizedBlocks.observe(this) { text ->
            showTextOnScreenshotWithInteraction(text)
        }

        // 摘要键逻辑
        val btnSummary = findViewById<ImageButton>(R.id.btnSummary)
        btnSummary.setOnClickListener {
            //TODO 摘要
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

        //设置二次裁剪功能监听器和交互逻辑
        setupTouchListener()      // 初始化触摸裁剪逻辑
        setupObservers()          // 观察 ViewModel 的选区状态

        //撤回和重做
        viewModel.currentBitmap.observe(this) { bitmap ->
            imageView.setImageBitmap(bitmap)
            graffitiView.setBitmap(bitmap)
            selectView.clearSelection()
            btnConfirmSelection.visibility = View.GONE
            findViewById<TextView>(R.id.selectionHint).visibility = View.VISIBLE
            Toast.makeText(this, "图像已更新", Toast.LENGTH_SHORT).show()
        }

        // 撤销键逻辑
        val btnUndo = findViewById<ImageButton>(R.id.btnUndo)
        btnUndo.setOnClickListener {
            viewModel.undo()
        }

        // 重做键逻辑
        val btnRedo = findViewById<ImageButton>(R.id.btnRedo)
        btnRedo.setOnClickListener {
            viewModel.redo()
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


    }

    //  二次裁剪的框选设置触摸监听器
    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        val imageContainer = findViewById<View>(R.id.imageContainer)
        imageContainer.post {
            imageContainer.setOnTouchListener { _, event ->
                if (viewModel.isSelectionEnabled.value != true) return@setOnTouchListener false

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> viewModel.startDrag(event.x, event.y)
                    MotionEvent.ACTION_MOVE -> if (viewModel.isDragging.value == true) viewModel.updateDrag(event.x, event.y)
                    MotionEvent.ACTION_UP -> {
                        viewModel.endDrag(event.x, event.y)

                        val drawable = imageView.drawable as? BitmapDrawable
                        val rect = viewModel.selectionRect.value
                        if (drawable != null && rect != null && !rect.isEmpty) {
                            val croppedBitmap = cropRepository.cropBitmap(drawable.bitmap, rect, imageView.imageMatrix)
                            if (croppedBitmap != null) {
                                imageView.setImageBitmap(croppedBitmap)
                                val newKey = BitmapCache.cacheBitmap(croppedBitmap)
                                intent.putExtra(EXTRA_SCREENSHOT_KEY, newKey)
                                ImageHistory.push(newKey)
                                selectView.clearSelection()
                                findViewById<TextView>(R.id.selectionHint).visibility = View.VISIBLE
                                graffitiView.setBitmap(croppedBitmap)
                                Toast.makeText(this, "已裁剪并更新图像", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "裁剪失败或区域无效", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "无效选区或图片为空", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                true
            }
        }
    }

    //  二次裁剪的框选设置ViewModel监听器
    private fun setupObservers() {
        viewModel.selectionRect.observe(this) { rect ->
            if (::selectView.isInitialized && rect != null) {
                selectView.setSelection(
                    PointF(rect.left.toFloat(), rect.top.toFloat()),
                    PointF(rect.right.toFloat(), rect.bottom.toFloat())
                )
            }
            findViewById<TextView>(R.id.selectionHint).visibility = if (rect == null || rect.isEmpty) View.VISIBLE else View.GONE
        }
    }

    //裁剪功能开关
    private fun toggleSelectionMode() {
        isSelectionEnabled = !isSelectionEnabled
        val btnIfCanSelect = findViewById<ImageButton>(R.id.btnIsCanSelect)
        viewModel.toggleSelectionEnabled()
        if (viewModel.isSelectionEnabled.value == true) {
            findViewById<TextView>(R.id.selectionHint).visibility = View.VISIBLE
            btnIfCanSelect.setImageResource(R.drawable.ic_reselect_on)
            Toast.makeText(this, "裁剪功能已开启", Toast.LENGTH_SHORT).show()
        } else {
            selectView.clearSelection()
            findViewById<TextView>(R.id.selectionHint).visibility = View.GONE
            btnIfCanSelect.setImageResource(R.drawable.ic_reselect)
            Toast.makeText(this, "裁剪功能已关闭", Toast.LENGTH_SHORT).show()
        }
    }

    //水印功能，暂不做拆分
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

    //OCR功能用，绘制OCR识别界面
    private fun showTextOnScreenshotWithInteraction(visionText: Text) {
        val textBlocks = visionText.textBlocks
        val rootView = overlayView.parent as? ViewGroup ?: return

        // 清除旧的 TextView
        textViews.forEach { rootView.removeView(it) }
        textViews.clear()

        val imageView = overlayView
        val imageWidth = imageView.width
        val imageHeight = imageView.height

        val matrix = imageView.imageMatrix
        val values = FloatArray(9).also { matrix.getValues(it) }

        for (textBlock in textBlocks) {
            val text = textBlock.text
            val boundingBox = textBlock.boundingBox ?: continue

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
            val extraHeight = (blockHeight * 0.3f).toInt()

            val layoutWidth = blockWidth
            val layoutHeight = blockHeight + paddingVertical * 2 + extraHeight

            val textView = TextView(this).apply {
                this.text = text
                setTextColor(Color.BLACK)
                setBackgroundColor(Color.parseColor("#E0F0FF"))
                setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
                gravity = Gravity.CENTER
                tag = "ocr_text"

                val area = layoutWidth * layoutHeight
                val estimatedTextSizePx = sqrt(area.toFloat()) * 0.2f
                val textSizeSp = estimatedTextSizePx / resources.displayMetrics.scaledDensity
                textSize = textSizeSp.coerceIn(12f, 36f)

                minimumWidth = 32.dpToPx()
                minimumHeight = 24.dpToPx()

                setOnClickListener {
                    viewModel.toggleSelectText(text)

                    // 动态修改背景色（使用最新 ViewModel 状态）
                    val selected = viewModel.selectedTexts.value.orEmpty()
                    setBackgroundColor(
                        if (selected.contains(text))
                            Color.parseColor("#AA66CCFF")
                        else
                            Color.parseColor("#E0F0FF")
                    )
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

    //OCR功能用，添加复制按钮
    private fun addCopyButton() {
        val rootView = overlayView.parent as? ViewGroup ?: return

        copyButton = Button(this).apply {
            text = "复制选中内容"

            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f.dpToPx()
                setColor(Color.WHITE)
            }

            setOnClickListener {
                val selected = viewModel.selectedTexts.value.orEmpty()
                if (selected.isEmpty()) {
                    Toast.makeText(this@ScreenshotActivity, "未选中任何内容", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val combinedText = selected.joinToString("\n")
                val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("Selected OCR Text", combinedText)
                clipboardManager.setPrimaryClip(clipData)

                Toast.makeText(this@ScreenshotActivity, "已复制${selected.size}条内容", Toast.LENGTH_SHORT).show()

                // 清空选中
                viewModel.clearSelectedTexts()

                // 清理 UI 元素
                (overlayView.parent as? ViewGroup)?.let { parent ->
                    textViews.forEach { parent.removeView(it) }
                    parent.removeView(this@apply)
                }
                textViews.clear()
            }
        }

        rootView.addView(copyButton, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            rightMargin = 30
            bottomMargin = 30
        })
    }

    //复制到剪切板用，这里不用拆分，不涉及数据操作
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

    //分享图片用，不用拆分
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

    //保存图片，不用拆分
    private fun saveCurrentImage() {
        val imageView = findViewById<ImageView>(R.id.imageViewScreenshot)
        val bitmap = (imageView.drawable as? BitmapDrawable)?.bitmap
            ?: run {
                Toast.makeText(this, "无法获取图片", Toast.LENGTH_SHORT).show()
                return
            }


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

                })
            }
            ControlViewStatus.OtherMode.ordinal -> {
                graffitiView.visibility = View.GONE
                // 显示其他模式
                exControlFrame.removeAllViews()
            }
        }
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
                        // 执行重做逻辑
                        viewModel.redo()
                        return true
                    } else if (isCtrlPressed) {
                        // 执行撤销逻辑
                        viewModel.undo()
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

}
