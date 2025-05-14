import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.electroboys.lightsnap.R
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

object QRScannerUtil {
    interface QRDialogListener {
        fun onIgnore()
        fun onCopyRequested(content: String)
    }

    fun detectQRCode(
        context: Context,
        bitmap: Bitmap,
        listener: QRDialogListener,
        onNoQRCode: () -> Unit
    ) {
        val scanner = BarcodeScanning.getClient()
        val image = InputImage.fromBitmap(bitmap, 0)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    showQRNotification(
                        context = context,
                        content = barcodes.first().rawValue ?: "",
                        listener = listener
                    )
                } else {
                    onNoQRCode()
                }
            }
            .addOnFailureListener { onNoQRCode() }
    }

    private fun showQRNotification(
        context: Context,
        content: String,
        listener: QRDialogListener
    ) {
        Dialog(context, R.style.QRDialogTheme).apply {
            // 基础设置
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_qr_detected)
            setCancelable(true)

            // 窗口定位和样式
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setGravity(Gravity.TOP or Gravity.END)
                attributes = attributes?.apply {
                    width = 180.dpToPx(context)
                    height = 180.dpToPx(context)
                    y = 16.dpToPx(context)
                    x = 16.dpToPx(context)
                }
            }
            // 设置二维码内容
            findViewById<TextView>(R.id.tv_qr_content)?.text =
                if (content.length > 30) "${content.substring(0, 30)}..." else content

            // 按钮事件
            findViewById<Button>(R.id.btn_ignore).setOnClickListener {
                listener.onIgnore()
                dismiss()
            }
            findViewById<Button>(R.id.btn_copy).setOnClickListener {
                listener.onCopyRequested(content)
                dismiss()
            }
        }.show()
    }

    private fun Int.dpToPx(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()
}