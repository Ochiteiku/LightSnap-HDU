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
            val daysThreshold = sharedPrefs.getInt("cleanup_deadline", 0)
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

    private suspend fun cleanOldScreenshots(daysThreshold: Int, option: String) {
        val sharedPrefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val savedUriStr = sharedPrefs.getString("screenshot_save_uri", null) ?: return
        val folderUri = savedUriStr.toUri()
        val folder = DocumentFile.fromTreeUri(this, folderUri) ?: return

        val now = System.currentTimeMillis()
        val thresholdMillis = daysThreshold * 24 * 60 * 60 * 1000L
        val format = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val uploadList = mutableListOf<DocumentFile>()
        var deletedCount = 0

        //由于需要保证删除和上传的原子性，所以这里先找出需要上传或删除的文件
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
                            uploadList.add(file)
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }

        //首先保证上传成功，然后再执行删除操作，COSUtil已对上传操作进行协程处理
        if (option == SettingsConstants.CLEANUP_DELANDUPLOAD && uploadList.isNotEmpty()) {
            for (file in uploadList) {
                val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                val remotePath = "$dateStr/${file.name}"
                val tempFile = File.createTempFile("upload_", file.name ?: "", cacheDir)

                // 将文件复制到本地临时文件进行上传操作
                contentResolver.openInputStream(file.uri)?.use { input ->
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                }

                val success = COSUtil.uploadFile(remotePath, tempFile)
                if (success && file.delete()) {
                    deletedCount++
                }
            }
        }

        // 主线程弹出提示
        withContext(Dispatchers.Main) {
            Toast.makeText(
                applicationContext,
                "清理完成，共删除 $deletedCount 张截图",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
