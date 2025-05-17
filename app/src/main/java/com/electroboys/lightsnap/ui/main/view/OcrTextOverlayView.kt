package com.electroboys.lightsnap.ui.main.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.google.mlkit.vision.text.Text
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt
import androidx.core.graphics.toColorInt

class OcrTextOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val textViews = mutableListOf<TextView>()
    private var copyButton: Button? = null
    private val selectedTexts = mutableSetOf<String>()

    fun renderOcrResults(
        visionText: Text,
        imageMatrix: Matrix,
        imageWidth: Int,
        imageHeight: Int,
        onCopied: (() -> Unit)? = null
    ) {
        // 清理旧内容
        removeAllViews()
        textViews.clear()
        selectedTexts.clear()

        val values = FloatArray(9).also { imageMatrix.getValues(it) }

        for (textBlock in visionText.textBlocks) {
            val text = textBlock.text
            val boundingBox = textBlock.boundingBox ?: continue

            val mappedLeft = (boundingBox.left * values[Matrix.MSCALE_X] + values[Matrix.MTRANS_X])
            val mappedTop = (boundingBox.top * values[Matrix.MSCALE_Y] + values[Matrix.MTRANS_Y])
            val mappedRight = (boundingBox.right * values[Matrix.MSCALE_X] + values[Matrix.MTRANS_X])
            val mappedBottom = (boundingBox.bottom * values[Matrix.MSCALE_Y] + values[Matrix.MTRANS_Y])

            val scaledLeft = max(0f, mappedLeft).toInt()
            val scaledTop = max(0f, mappedTop).toInt()
            val scaledRight = min(mappedRight, imageWidth.toFloat()).toInt()
            val scaledBottom = min(mappedBottom, imageHeight.toFloat()).toInt()

            val blockWidth = scaledRight - scaledLeft
            val blockHeight = scaledBottom - scaledTop
            val extraHeight = (blockHeight * 0.3f).toInt()

            val layoutWidth = blockWidth
            val layoutHeight = blockHeight + 2.dpToPx() * 2 + extraHeight

            val textView = TextView(context).apply {
                this.text = text
                setTextColor(Color.BLACK)
                setBackgroundColor("#E0F0FF".toColorInt())
                setPadding(4.dpToPx(), 2.dpToPx(), 4.dpToPx(), 2.dpToPx())
                gravity = Gravity.CENTER
                tag = "ocr_text"

                val area = layoutWidth * layoutHeight
                val estimatedTextSizePx = sqrt(area.toFloat()) * 0.2f
                val textSizeSp = estimatedTextSizePx / resources.displayMetrics.scaledDensity
                textSize = textSizeSp.coerceIn(12f, 36f)

                minimumWidth = 32.dpToPx()
                minimumHeight = 24.dpToPx()

                setOnClickListener {
                    if (selectedTexts.contains(text)) {
                        selectedTexts.remove(text)
                        setBackgroundColor("#E0F0FF".toColorInt())
                    } else {
                        selectedTexts.add(text)
                        setBackgroundColor("#AA66CCFF".toColorInt())
                    }
                }
            }

            addView(textView, LayoutParams(layoutWidth, layoutHeight).apply {
                leftMargin = scaledLeft
                topMargin = scaledTop
            })

            textViews.add(textView)
        }

        addCopyButton(onCopied)
    }

    private fun addCopyButton(onCopied: (() -> Unit)?) {
        val button = Button(context).apply {
            text = "复制选中内容"
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f.dpToPx().toFloat()
                setColor(Color.WHITE)
            }

            setOnClickListener {
                if (selectedTexts.isEmpty()) {
                    Toast.makeText(context, "未选中任何内容", Toast.LENGTH_SHORT).show()
                    onCopied?.invoke()
                    return@setOnClickListener
                }

                val combinedText = selectedTexts.joinToString("\n")
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Selected OCR Text", combinedText))

                Toast.makeText(context, "已复制 ${selectedTexts.size} 条内容", Toast.LENGTH_SHORT).show()

                // 清空状态
                selectedTexts.clear()
                onCopied?.invoke()
            }
        }

        addView(button, LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            rightMargin = 30.dpToPx()
            bottomMargin = 42.dpToPx()
        })

        copyButton = button
    }

    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).roundToInt()

    private fun Float.dpToPx(): Float = this * resources.displayMetrics.density
}
