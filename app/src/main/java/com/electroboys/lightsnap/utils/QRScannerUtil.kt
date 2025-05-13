import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

object QRScannerUtil {

    fun detectQRCode(
        context: Context,
        bitmap: Bitmap,
        onContinueScreenshot: () -> Unit
    ) {
        val scanner = BarcodeScanning.getClient()
        val image = InputImage.fromBitmap(bitmap, 0)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val qrContent = barcodes.first().rawValue ?: ""
                    showQRResultDialog(context, qrContent, onContinueScreenshot)
                } else {
                    onContinueScreenshot()
                }
            }
            .addOnFailureListener {
                onContinueScreenshot()
            }
    }

    private fun showQRResultDialog(
        context: Context,
        qrContent: String,
        onContinueScreenshot: () -> Unit
    ) {
        val displayText = if (qrContent.length > 50) {
            "${qrContent.substring(0, 50)}..."
        } else {
            qrContent
        }

        // 创建对话框但不立即显示
        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle("检测到二维码内容")
            .setMessage(displayText)
            // 1. 忽略按钮（继续进入截图编辑）
            .setNeutralButton("忽略") { dialog, _ ->
                dialog.dismiss()
                onContinueScreenshot()
            }
            // 2. 返回按钮（取消对话框，不做任何操作）
            .setNegativeButton("返回") { dialog, _ ->
                dialog.dismiss()
            }
            // 3. 复制（先设置 PositiveButton 为 null，后面再写监听事件）
            .setPositiveButton("复制内容", null)
            .create()

        // 显示对话框
        dialog.show()

        // 获取 PositiveButton 并重新设置点击事件
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val clipboard =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("二维码内容", qrContent)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
            // 注意：这里不调用dialog.dismiss()
        }
    }
}