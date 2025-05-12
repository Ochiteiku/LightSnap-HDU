package com.electroboys.lightsnap.ui.main.activity


import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.electroboys.lightsnap.R
import androidx.core.net.toUri
import com.electroboys.lightsnap.ui.main.activity.BaseActivity.BaseActivity


class VideoPlayActivity : BaseActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videoplay)

        // 设置Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 显示返回按钮
        supportActionBar?.title = "播放视频"

        val videoView = findViewById<VideoView>(R.id.videoView)

        val videoUri = "https://lightsnap-1318767045.cos.ap-shanghai.myqcloud.com/test_video.mp4".toUri()
        videoView.setVideoURI(videoUri)

        videoView.setOnPreparedListener {
            videoView.start()
        }
    }

    // 处理返回按钮点击事件
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish() // 关闭当前Activity
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}