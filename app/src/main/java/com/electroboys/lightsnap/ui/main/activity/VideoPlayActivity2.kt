package com.electroboys.lightsnap.ui.main.activity

import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.electroboys.lightsnap.R
import androidx.core.net.toUri
import com.electroboys.lightsnap.ui.main.activity.BaseActivity.BaseActivity

class VideoPlayActivity2 : BaseActivity() {
    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videoplay2)

        // 初始化 PlayerView
        playerView = findViewById(R.id.player_view)

        // 初始化 ExoPlayer
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        // 构建 Uri 指向 raw 文件
        val videoUri = "android.resource://${packageName}/${R.raw.test_video}".toUri()
        val mediaItem = MediaItem.fromUri(videoUri)

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    override fun onStop() {
        super.onStop()
        player.release()
    }
}