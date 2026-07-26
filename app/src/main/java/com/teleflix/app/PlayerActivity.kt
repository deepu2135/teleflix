package com.teleflix.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private var videoUrl: String = ""
    private var videoTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        videoUrl = intent.getStringExtra("VIDEO_URL") ?: ""
        videoTitle = intent.getStringExtra("VIDEO_TITLE") ?: "Video Stream"

        val rootView = android.widget.RelativeLayout(this).apply {
            setBackgroundColor(android.graphics.Color.BLACK)
        }

        playerView = PlayerView(this).apply {
            layoutParams = android.widget.RelativeLayout.LayoutParams(
                android.widget.RelativeLayout.LayoutParams.MATCH_PARENT,
                android.widget.RelativeLayout.LayoutParams.MATCH_PARENT
            )
        }
        rootView.addView(playerView)

        // Title Overlay Bar
        val topBar = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            setBackgroundColor(android.graphics.Color.parseColor("#88000000"))
            setPadding(32, 32, 32, 32)
            layoutParams = android.widget.RelativeLayout.LayoutParams(
                android.widget.RelativeLayout.LayoutParams.MATCH_PARENT,
                android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val titleView = TextView(this).apply {
            text = videoTitle
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.WHITE)
            layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val vlcButton = Button(this).apply {
            text = "VLC"
            setBackgroundColor(android.graphics.Color.parseColor("#EA580C"))
            setTextColor(android.graphics.Color.WHITE)
            setOnClickListener {
                openExternalPlayer("org.videolan.vlc")
            }
        }

        topBar.addView(titleView)
        topBar.addView(vlcButton)
        rootView.addView(topBar)

        setContentView(rootView)

        initializePlayer()
    }

    private fun initializePlayer() {
        if (videoUrl.isEmpty()) return
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            playerView.player = exoPlayer
            val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    private fun openExternalPlayer(packageName: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(videoUrl), "video/*")
            if (packageName.isNotEmpty()) {
                setPackage(packageName)
            }
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "External player not installed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
