package com.electroboys.lightsnap.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.electroboys.lightsnap.data.entity.SettingsConstants
import com.electroboys.lightsnap.utils.COSUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScreenshotCleanupService : Service() {

    companion object {
        private const val CHANNEL_ID = "cleanup_channel"
        private const val NOTIFICATION_ID = 102

        fun startService(context: Context) {
            val intent = Intent(context, ScreenshotCleanupService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 使用协程代替线程
        serviceScope.launch {
            val sharedPrefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
            val option = sharedPrefs.getString("cleanup", SettingsConstants.CLEANUP_OFF)
            val daysThreshold = sharedPrefs.getInt("cleanup_deadline", 30)
            if (option != SettingsConstants.CLEANUP_OFF) {
                cleanOldScreenshots(daysThreshold, option.toString())
            }
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("清理截图中")
            .setContentText("正在后台清理旧截图")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "截图清理服务",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    //清理主要逻辑
    private suspend fun cleanOldScreenshots(daysThreshold: Int, option: String) {
        val sharedPrefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val savedUriStr = sharedPrefs.getString("screenshot_save_uri", null) ?: return
        val uri = savedUriStr.toUri()

        val now = System.currentTimeMillis()
        val thresholdMillis = daysThreshold * 24 * 60 * 60 * 1000L
        val format = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        var deletedCount = 0

        //这里是为了适配两种不同的uri格式，SAF路径和普通路径
        if (uri.scheme == "content") {
            val folder = DocumentFile.fromTreeUri(this, uri) ?: return
            for (file in folder.listFiles()) {
                val name = file.name ?: continue
                val match = Regex("""Img_(\d{8}_\d{6})\.(jpg|png)""", RegexOption.IGNORE_CASE).matchEntire(name)
                    ?: continue
                try {
                    val date = format.parse(match.groupValues[1]) ?: continue
                    if (now - date.time > thresholdMillis) {
                        when (option) {
                            SettingsConstants.CLEANUP_DEL -> {
                                if (file.delete()) deletedCount++
                            }
                            SettingsConstants.CLEANUP_DELANDUPLOAD -> {
                                // 上传 -> 成功 -> 删除
                                val remotePath = "${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}/${file.name}"
                                val tempFile = convertDocumentFileToTempFile(file)
                                val success = COSUtil.uploadFile(remotePath, tempFile)
                                if (success && file.delete()) {
                                    deletedCount++
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                }
            }
        } else {
            val folder = File(uri.path ?: return)
            if (!folder.exists() || !folder.isDirectory) return

            for (file in folder.listFiles() ?: emptyArray()) {
                if (!file.isFile) continue
                val name = file.name
                val match = Regex("""Img_(\d{8}_\d{6})\.(jpg|png)""", RegexOption.IGNORE_CASE).matchEntire(name)
                    ?: continue
                try {
                    val date = format.parse(match.groupValues[1]) ?: continue
                    if (now - date.time > thresholdMillis) {
                        when (option) {
                            SettingsConstants.CLEANUP_DEL -> {
                                if (file.delete()) deletedCount++
                            }

                            SettingsConstants.CLEANUP_DELANDUPLOAD -> {
                                val remotePath = "${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}/${file.name}"
                                val success = COSUtil.uploadFile(remotePath, file)
                                if (success && file.delete()) {
                                    deletedCount++
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }

        withContext(Dispatchers.Main) {
            Toast.makeText(
                applicationContext,
                "清理完成，共清理 $deletedCount 张截图",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // 用于将 SAF 的 DocumentFile 转为临时 File 上传
    private fun convertDocumentFileToTempFile(documentFile: DocumentFile): File {
        val tempFile = File.createTempFile("upload_", documentFile.name ?: "temp.png")
        val inputStream = applicationContext.contentResolver.openInputStream(documentFile.uri)
        inputStream?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

}
