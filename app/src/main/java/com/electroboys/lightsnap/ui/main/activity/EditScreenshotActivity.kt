package com.electroboys.lightsnap.ui.main.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.domain.screenshot.BitmapCache
import java.io.File

class EditScreenshotActivity : AppCompatActivity() {

    private lateinit var btnAddText: Button
    private lateinit var screentShot: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        btnAddText = findViewById(R.id.btnAddText)
        screentShot = findViewById(R.id.imageViewScreenshot)

        // 从缓存中读取bitmap，并将其删除
        val screenshotPath = intent.getStringExtra("image_path")
        val bitmap = BitmapFactory.decodeFile(screenshotPath)
        File(screenshotPath).delete()
        screentShot.setImageBitmap(bitmap)

    }
}