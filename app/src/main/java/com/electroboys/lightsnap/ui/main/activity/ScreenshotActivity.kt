package com.electroboys.lightsnap.ui.main.activity

import QRScannerUtil
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.data.entity.SettingsConstants
import com.electroboys.lightsnap.data.screenshot.BitmapCache
import com.electroboys.lightsnap.data.screenshot.ControlViewStatus
import com.electroboys.lightsnap.data.screenshot.ImageHistory
import com.electroboys.lightsnap.domain.screenshot.ControlPanelManager
import com.electroboys.lightsnap.domain.screenshot.ModeActions
import com.electroboys.lightsnap.domain.screenshot.ModeManager
import com.electroboys.lightsnap.domain.screenshot.repository.ImageCropRepository
import com.electroboys.lightsnap.domain.screenshot.repository.OcrRepository
import com.electroboys.lightsnap.domain.screenshot.watermark.WatermarkConfig
import com.electroboys.lightsnap.ui.main.view.FrameSelectView
import com.electroboys.lightsnap.ui.main.view.GraffitiView
import com.electroboys.lightsnap.ui.main.view.OcrTextOverlayView
import com.electroboys.lightsnap.ui.main.view.SelectView
import com.electroboys.lightsnap.ui.main.view.WatermarkOverlayView
import com.electroboys.lightsnap.ui.main.view.WatermarkSettingBarView
import com.electroboys.lightsnap.ui.main.viewmodel.ScreenshotViewModel
import com.electroboys.lightsnap.ui.main.viewmodel.factory.ScreenshotViewModelFactory
import com.electroboys.lightsnap.utils.BaiduTranslator
import com.electroboys.lightsnap.utils.ClipboardUtil
import com.electroboys.lightsnap.utils.ShareImageUtils
import com.electroboys.lightsnap.utils.SummaryDialogUtils
import com.google.mlkit.vision.text.Text


class ScreenshotActivity : AppCompatActivity(), ModeActions {

    private lateinit var controlPanelManager: ControlPanelManager

    private lateinit var graffitiView: GraffitiView
    private lateinit var exControlFrame: FrameLayout // 扩展控制面板
    private lateinit var selectView: SelectView
    private lateinit var imageView: ImageView
    private lateinit var ocrOverlayView: OcrTextOverlayView // ocr展示用界面
    private lateinit var btnConfirmSelection: ImageButton
    private lateinit var frameSelectView: FrameSelectView

    private lateinit var watermarkOverlay: WatermarkOverlayView
    private lateinit var watermarkSettingBar: WatermarkSettingBarView
    private var isWatermark = false

    //按钮组
    private lateinit var btnWatermark: ImageButton //水印按钮
    private lateinit var btnIfCanSelect: ImageButton //二次选择
    private lateinit var btnText: ImageButton //添加文字
    private lateinit var btnGraffiti: ImageButton //添加涂鸦
    private lateinit var btnMosaic: ImageButton //添加马赛克
    private lateinit var btnArrow: ImageButton //添加箭头
    private lateinit var btnBox: ImageButton // 添加方框


    companion object {
        const val EXTRA_SCREENSHOT_KEY = "screenshot_key"

        //所有的功能列表
        enum class Mode {
            None,
            AddText,
            Graffiti,
            Arrow,
            Mosaic,
            Crop,
            OCR,
            Framing
        }
    }

    private lateinit var modeManager: ModeManager//模式管理器
    private var originalBitmapKey: String? = null
    private val watermarkConfig = WatermarkConfig.default() //水印配置
    private lateinit var overlayView: ImageView

    // 初始化 ViewModel
    private lateinit var viewModel: ScreenshotViewModel
    private val cropRepository = ImageCropRepository()

    private var dotCount = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var textViewSummaryStatus: TextView

    private val updateDotsRunnable = object : Runnable {
        override fun run() {
            val text = "摘要生成中" + ".".repeat(dotCount)
            textViewSummaryStatus.text = text
            dotCount = (dotCount + 1) % 4
            handler.postDelayed(this, 500)
        }
    }

    private fun startSummaryLoading() {
        dotCount = 1
        handler.post(updateDotsRunnable)
        textViewSummaryStatus.visibility = View.VISIBLE
    }

    private fun stopSummaryLoading() {
        handler.removeCallbacks(updateDotsRunnable)
        textViewSummaryStatus.text = "摘要生成完成"
        handler.postDelayed({
            textViewSummaryStatus.visibility = View.GONE
        }, 1500)
    }

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
        frameSelectView = findViewById(R.id.frameSelectView)
        ocrOverlayView = findViewById(R.id.ocrOverlayView)
        overlayView = imageView


        // OCR键逻辑
        val btnOcr = findViewById<ImageButton>(R.id.btnOcr)
        btnOcr.setOnClickListener {
            modeManager.enter(Mode.OCR)
        }
        viewModel.recognizedBlocks.observe(this) { text ->
            ocrOverlayView.visibility = View.VISIBLE

            ocrOverlayView.renderOcrResults(
                visionText = text,
                imageMatrix = overlayView.imageMatrix,
                imageWidth = overlayView.width,
                imageHeight = overlayView.height
            ) {
                // 复制成功后回调，可做清理或关闭页面
                ocrOverlayView.visibility = View.GONE
            }
        }

        // 摘要键逻辑
        val btnSummary = findViewById<ImageButton>(R.id.btnSummary)
        textViewSummaryStatus = findViewById<TextView>(R.id.textViewSummaryStatus)
        btnSummary.setOnClickListener {
            viewModel.isGeneratingSummary.observe(this) { isLoading ->
                if (isLoading) {
                    startSummaryLoading()
                } else {
                    stopSummaryLoading()
                }
            }
            viewModel.summaryText.observe(this) { summary ->
                if (summary.isNotBlank()) {
                    showSummaryDialog(summary)
                }
            }
            // 传入当前截图 bitmap，执行识别 + 摘要流程
            val currentBitmap = getcurrentBitmap()
            if (currentBitmap != null) {
                viewModel.recognizeAndSummarize(currentBitmap)
            }
        }

        // 添加文字键逻辑
        btnText = findViewById<ImageButton>(R.id.btnText)
        btnText.setOnClickListener {
            modeManager.enter(Mode.AddText)
        }

        //撤回和重做
        viewModel.currentBitmap.observe(this) { bitmap ->
            imageView.setImageBitmap(bitmap)
            graffitiView.setBitmap(bitmap)
            frameSelectView.setBitmap(bitmap)
            selectView.clearSelection()
            btnConfirmSelection.visibility = View.GONE
        }

        // 撤销键逻辑
        val btnUndo = findViewById<ImageButton>(R.id.btnUndo)
        btnUndo.setOnClickListener {
            modeManager.enter(Mode.None)
            viewModel.undo()
        }

        // 重做键逻辑
        val btnRedo = findViewById<ImageButton>(R.id.btnRedo)
        btnRedo.setOnClickListener {
            modeManager.enter(Mode.None)
            viewModel.redo()
        }

        // 转发键逻辑
        val btnShare = findViewById<ImageButton>(R.id.btnShare)
        btnShare.setOnClickListener {
            modeManager.enter(Mode.None)
            shareCurrentImage()
        }

        // 图片复制键逻辑
        val btnCopy = findViewById<ImageButton>(R.id.btnCopy)
        btnCopy.setOnClickListener {
            modeManager.enter(Mode.None)
            var bitmap = getcurrentBitmap()
            if (isWatermark){
                bitmap = bitmap?.let { it1 -> watermarkOverlay.applyWatermarkToBitmap(it1) }
            }
            // 执行复制图片操作
            if (bitmap != null) {
                ClipboardUtil.copyBitmapToClipboard(this, bitmap)
            }
            Toast.makeText(this, "截图已复制到剪贴板", Toast.LENGTH_SHORT).show()
            finish()
        }

        // 保存键逻辑
        val btnSave = findViewById<ImageButton>(R.id.btnSave)
        btnSave.setOnClickListener {
            modeManager.enter(Mode.None)
            saveCurrentImage()
        }

        // 退出键逻辑
        val btnExit = findViewById<ImageButton>(R.id.btnExit)
        btnExit.setOnClickListener {
            modeManager.enter(Mode.None)
            finish()
        }

        //  裁剪开关逻辑
        btnIfCanSelect = findViewById<ImageButton>(R.id.btnIsCanSelect)
        btnIfCanSelect.setOnClickListener {
            modeManager.enter(Mode.Crop)
        }

        // 涂鸦按钮逻辑
        btnGraffiti = findViewById<ImageButton>(R.id.btnDraw)
        btnGraffiti.setOnClickListener {
            modeManager.enter(Mode.Graffiti)
        }

        // 马赛克按钮逻辑
        btnMosaic = findViewById<ImageButton>(R.id.btnMosaic)
        btnMosaic.setOnClickListener {
            modeManager.enter(Mode.Mosaic)
        }

        // 箭头按钮逻辑
        btnArrow = findViewById<ImageButton>(R.id.btnArrow)
        btnArrow.setOnClickListener {
            modeManager.enter(Mode.Arrow)
        }

        //钉选逻辑
        findViewById<View>(R.id.btnFixed).setOnClickListener {
            SettingsConstants.PicIsHangUp = true
            val bitmap = getcurrentBitmap()
            val bitmapKey = bitmap?.let { it1 -> BitmapCache.cacheBitmap(it1) }
            SettingsConstants.floatBitmapKey = bitmapKey
            finish()
        }

        val btnTranslate = findViewById<View>(R.id.btnTranslate)
        btnTranslate.setOnClickListener {
            val bitmap = getcurrentBitmap()
            bitmap?.let { it1 ->
                viewModel.recognizeAndCallback(
                    it1,
                    object : ScreenshotViewModel.OnTextRecognizedListener {
                        override suspend fun onTextRecognized(text: Text?) {
                            text?.text?.let { it2 ->
                                BaiduTranslator.translate(
                                    this@ScreenshotActivity, it2,
                                    object : BaiduTranslator.TranslationCallback {
                                        override fun onSuccess(translatedText: String) {
                                            imageView.post {
                                                Toast.makeText(
                                                    baseContext,
                                                    "翻译成功",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                        override fun onFailure(error: String) {
                                            imageView.post {
                                                Toast.makeText(
                                                    baseContext,
                                                    "翻译失败" + error,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    })
                            }
                        }
                    })
            }
        }

        btnBox = findViewById<ImageButton>(R.id.btnBox)
        btnBox.setOnClickListener {
            modeManager.enter(Mode.Framing)
        }

        graffitiView.setOnBitmapChangeListener(object : GraffitiView.onBitmapChangeListener {
            override fun onBitmapChange(bitmap: Bitmap) {
                setcurrentBitmapandRefreshKey(bitmap)
            }
        })
        frameSelectView.setOnBitmapChangeListener(object : GraffitiView.onBitmapChangeListener {
            override fun onBitmapChange(bitmap: Bitmap) {
                setcurrentBitmapandRefreshKey(bitmap)
            }
        })

        // 水印开关逻辑
        btnWatermark = findViewById(R.id.btnWatermark)
        btnWatermark.setOnClickListener {
            if (watermarkOverlay.isVisible) {
                // 关闭水印
                isWatermark = false
                watermarkSettingBar.updateUIState(false)
                watermarkOverlay.visibility = View.GONE
                watermarkSettingBar.updateUIState(false)
                btnWatermark.setImageResource(R.drawable.ic_watermark)
            } else {
                // 开启水印
                isWatermark = true
                watermarkSettingBar.updateUIState(true)
                watermarkOverlay.visibility = View.VISIBLE
                watermarkSettingBar.updateUIState(true)
                btnWatermark.setImageResource(R.drawable.ic_watermark_on)
            }
        }
        //水印工具栏初始化
        watermarkSettingBar = WatermarkSettingBarView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                this.gravity = Gravity.BOTTOM or Gravity.END
            }
            // 监听文字变化
            onTextChanged = { text ->
                watermarkConfig.setText(text)
                watermarkOverlay.setWatermark(watermarkConfig) // 刷新 overlay
            }
            // 监听透明度变化
            onAlphaChanged = { alpha ->
                watermarkConfig.setAlpha(alpha)
                watermarkOverlay.setWatermark(watermarkConfig) // 刷新 overlay
            }
        }

        findViewById<FrameLayout>(R.id.imageContainer).addView(watermarkSettingBar)
        watermarkSettingBar.updateUIState(false)

        // 获取传入的 key
        val key = intent.getStringExtra(EXTRA_SCREENSHOT_KEY)
        originalBitmapKey = key
        key?.let {
            ImageHistory.push(it)
        }

        // 从缓存中取出 Bitmap
        val bitmap = key?.let { BitmapCache.getBitmap(it) }
        imageView.setImageBitmap(bitmap)

        // QR按键逻辑
        val btnQR = findViewById<ImageButton>(R.id.btnQR)
        btnQR.setOnClickListener {
            val currentBitmap = (imageView.drawable as? BitmapDrawable)?.bitmap
            if (currentBitmap != null) {
                QRScannerUtil.detectQRCode(
                    context = this,
                    bitmap = currentBitmap,
                    listener = object : QRScannerUtil.QRDialogListener {
                        override fun onIgnore() {
                            // 用户忽略二维码的处理
                        }

                        override fun onCopyRequested(content: String) {
                            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("二维码内容", content))
                            Toast.makeText(
                                this@ScreenshotActivity,
                                "已复制二维码内容", Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onNoQRCode = {
                        // 未检测到二维码时显示Toast
                        Toast.makeText(this, "未检测到二维码", Toast.LENGTH_SHORT).show()
                    }
                )
            }else{
                Toast.makeText(this, "无图片", Toast.LENGTH_SHORT).show()
            }
        }

        if (bitmap != null) {
            graffitiView.setBitmap(bitmap)

            // QR
            QRScannerUtil.detectQRCode(
                context = this,
                bitmap = bitmap,
                listener = object : QRScannerUtil.QRDialogListener {
                    override fun onIgnore() {
                        // 用户点击忽略，不做任何操作
                    }

                    override fun onCopyRequested(content: String) {
                        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("二维码内容", content))
                        Toast.makeText(
                            this@ScreenshotActivity,
                            "已复制二维码内容", Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onNoQRCode = {
                    // 无二维码时直接进入编辑模式
                    modeManager.enter(Mode.Crop)
                }
            )

        } else {
            Toast.makeText(this, "截图数据为空或已释放", Toast.LENGTH_SHORT).show()
        }

        //  由于裁剪功能默认时开启的，在所有组件完成初始化之后进入裁剪模式
        modeManager = ModeManager(this)
        controlPanelManager = ControlPanelManager(
            context = this,
            imageView = imageView,
            graffitiView = graffitiView,
            frameSelectView = frameSelectView,
            exControlFrame = exControlFrame,
            intent = intent,
            container = findViewById(R.id.imageContainer),
            btnText = btnText,
            cropRepository = cropRepository,
            imageContainer = findViewById(R.id.imageContainer),
            selectView = selectView,
            viewModel = viewModel,
            selectionHintView = findViewById(R.id.selectionHint)
        )
    }

    //分享图片用
    private fun shareCurrentImage() {
        var bitmap = getcurrentBitmap()
        if(isWatermark){
            bitmap = bitmap?.let { watermarkOverlay.applyWatermarkToBitmap(it) }
        }
        if (bitmap != null) {
            ShareImageUtils.shareBitmap(this, bitmap)
        }
    }

    //保存图片
    private fun saveCurrentImage() {
        var bitmap = getcurrentBitmap()
        if (bitmap == null) {
            Toast.makeText(this, "图片不存在", Toast.LENGTH_SHORT).show()
            return
        }
        if(isWatermark){
            bitmap = bitmap.let { watermarkOverlay.applyWatermarkToBitmap(it) }
        }
        setcurrentBitmapandRefreshKey(bitmap)

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

    // 显示摘要
    private fun showSummaryDialog(summary: String) {
        SummaryDialogUtils.showSummaryDialog(this, summary)
    }

    private fun getcurrentBitmap(): Bitmap? {
        return (imageView.drawable as? BitmapDrawable)?.bitmap
    }

    private fun setcurrentBitmapandRefreshKey(bitmap: Bitmap?) {
        imageView.setImageBitmap(bitmap)
        val newkey = bitmap?.let { BitmapCache.cacheBitmap(it) }
        if (newkey != null) {
            ImageHistory.push(newkey)
        }
        intent.putExtra(EXTRA_SCREENSHOT_KEY, newkey)
    }
    override fun onDestroy() {
        super.onDestroy()
        // 清理 Bitmap 缓存
        if (!SettingsConstants.PicIsHangUp) {
            val currentKey = intent.getStringExtra(EXTRA_SCREENSHOT_KEY)
            BitmapCache.clearExcept(currentKey)
            handler.removeCallbacks(updateDotsRunnable)
        }

    }

    // OnAndOffMode
    // 重写ModeActions中的Actions方法以改变模式
    override fun enterGraffiti() {
        updateModeButtonIcons(Mode.Graffiti)
        showControlPanel(ControlViewStatus.GraffitiMode, Mode.Graffiti)
    }

    override fun exitGraffiti() {
        updateModeButtonIcons(Mode.None)
    }

    override fun enterAddText() {
        updateModeButtonIcons(Mode.AddText)
        showControlPanel(ControlViewStatus.AddTextMode, Mode.AddText)
    }

    override fun exitAddText() {
        controlPanelManager.exitAddTextMode()
        updateModeButtonIcons(Mode.None)
    }

    override fun enterMosaic() {
        updateModeButtonIcons(Mode.Mosaic)
        showControlPanel(ControlViewStatus.MosaicMode, Mode.Mosaic)
    }

    override fun exitMosaic() {
        updateModeButtonIcons(Mode.None)
    }

    override fun enterArrow() {
        updateModeButtonIcons(Mode.Arrow)
        showControlPanel(ControlViewStatus.ArrowMode, Mode.Arrow)
    }

    override fun exitArrow() {
        updateModeButtonIcons(Mode.None)
    }

    override fun enterCrop() {
        updateModeButtonIcons(Mode.Crop)
        showControlPanel(ControlViewStatus.CropMode, Mode.Crop)
    }

    override fun exitCrop() {
        controlPanelManager.exitCropMode()
        updateModeButtonIcons(Mode.None)
    }

    override fun enterBox() {
        updateModeButtonIcons(Mode.Framing)
        showControlPanel(ControlViewStatus.FramingMode, Mode.Framing)
    }

    override fun exitBox() {
        updateModeButtonIcons(Mode.None)
    }

    override fun onEnterOCR() {
        updateModeButtonIcons(Mode.OCR)
        getcurrentBitmap()?.let { viewModel.recognize(it) }
    }

    override fun showControlPanel(mode: ControlViewStatus, activeMode: Mode) {
        controlPanelManager.applyMode(mode)
        updateModeButtonIcons(activeMode)
    }
    // 快捷键触发功能
    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
        if (event.action == android.view.KeyEvent.ACTION_DOWN) {
            val isCtrlPressed = event.isCtrlPressed
            val isShiftPressed = event.isShiftPressed
            when (event.keyCode) {
                android.view.KeyEvent.KEYCODE_C -> {
                    if (isCtrlPressed) {
                        getcurrentBitmap()?.let { ClipboardUtil.copyBitmapToClipboard(this, it) }
                        Toast.makeText(this, "截图已复制到剪贴板", Toast.LENGTH_SHORT).show()
                        finish()
                        return true
                    }
                }

                android.view.KeyEvent.KEYCODE_Z -> {
                    if (isCtrlPressed && isShiftPressed) {
                        viewModel.redo()  //重做操作
                        return true
                    } else if (isCtrlPressed) {
                        viewModel.undo() //撤销操作
                        return true
                    }
                }

                android.view.KeyEvent.KEYCODE_ESCAPE -> {
                    finish() //退出截图流程
                    return true
                }

                android.view.KeyEvent.KEYCODE_ENTER -> {
                    saveCurrentImage() //保存操作
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    //更新按钮状态
    private fun updateModeButtonIcons(activeMode: Mode) {
        btnText.setImageResource(
            if (activeMode == Mode.AddText) R.drawable.ic_text_on
            else R.drawable.ic_text
        )

        btnIfCanSelect.setImageResource(
            if (activeMode == Mode.Crop) R.drawable.ic_reselect_on
            else R.drawable.ic_reselect
        )

        btnGraffiti.setImageResource(
            if (activeMode == Mode.Graffiti) R.drawable.ic_draw_on
            else R.drawable.ic_draw
        )

        btnMosaic.setImageResource(
            if (activeMode == Mode.Mosaic) R.drawable.ic_mask_on
            else R.drawable.ic_mask
        )

        btnArrow.setImageResource(
            if (activeMode == Mode.Arrow) R.drawable.ic_arrow_on
            else R.drawable.ic_arrow
        )

        btnBox.setImageResource(
            if (activeMode == Mode.Framing) R.drawable.ic_box_on
            else R.drawable.ic_box
        )
    }
}
