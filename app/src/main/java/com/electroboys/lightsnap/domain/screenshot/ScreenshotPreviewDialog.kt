package com.electroboys.lightsnap.domain.screenshot

import com.electroboys.lightsnap.R
import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class ScreenshotPreviewDialog(private val bitmap: Bitmap) : DialogFragment() {

    private var onConfirmListener: (() -> Unit)? = null
    private var onCancelListener: (() -> Unit)? = null
    private var onEditListener: ((Bitmap) -> Unit)? = null

    // 设置回调监听器
    fun setOnConfirmListener(listener: () -> Unit): ScreenshotPreviewDialog {
        this.onConfirmListener = listener
        return this
    }

    fun setOnCancelListener(listener: () -> Unit): ScreenshotPreviewDialog {
        this.onCancelListener = listener
        return this
    }

    fun setOnEditListener(listener: (Bitmap) -> Unit): ScreenshotPreviewDialog {
        this.onEditListener = listener
        return this
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_screenshot_preview, null)

        val imageView = view.findViewById<ImageView>(R.id.previewImageView)
        imageView.setImageBitmap(bitmap)

        val btnConfirm = view.findViewById<Button>(R.id.btnConfirm)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnEdit = view.findViewById<Button>(R.id.btnEdit)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("截图预览")
            .create()

        btnConfirm.setOnClickListener {
            onConfirmListener?.invoke()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            onCancelListener?.invoke()
            dialog.dismiss()
        }

        btnEdit.setOnClickListener {
            onEditListener?.invoke(bitmap)
            dialog.dismiss()
        }

        return dialog
    }
}
