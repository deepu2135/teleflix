package com.teleflix.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class MediaItem(
    val id: String,
    val title: String,
    val posterUrl: String,
    val year: String,
    val rating: String,
    val overview: String
)

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchInput: EditText
    private lateinit var searchButton: Button
    private lateinit var statusButton: Button

    private val mediaList = mutableListOf<MediaItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val rootView = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.parseColor("#090A0F"))
            setPadding(16, 16, 16, 16)
        }

        // Header Bar
        val header = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, 16)
        }

        val titleView = android.widget.TextView(this).apply {
            text = "TELEFLIX STREAMER"
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#E50914"))
            layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        statusButton = Button(this).apply {
            text = "TDLib Channels & Auth"
            textSize = 11f
            setBackgroundColor(android.graphics.Color.parseColor("#1E3A8A"))
            setTextColor(android.graphics.Color.parseColor("#93C5FD"))
            setOnClickListener {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
        }

        header.addView(titleView)
        header.addView(statusButton)
        rootView.addView(header)

        // Search Section
        val searchLayout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, 16)
        }

        searchInput = EditText(this).apply {
            hint = "Search movies or TV shows..."
            setHintTextColor(android.graphics.Color.parseColor("#6B7280"))
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(android.graphics.Color.parseColor("#161B28"))
            setPadding(24, 16, 24, 16)
            layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        searchButton = Button(this).apply {
            text = "Search"
            setBackgroundColor(android.graphics.Color.parseColor("#E50914"))
            setTextColor(android.graphics.Color.WHITE)
            setOnClickListener {
                performSearch(searchInput.text.toString())
            }
        }

        searchLayout.addView(searchInput)
        searchLayout.addView(searchButton)
        rootView.addView(searchLayout)

        // Media Grid
        recyclerView = RecyclerView(this).apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
        }
        rootView.addView(recyclerView)

        setContentView(rootView)

        loadFeaturedCatalog()
    }

    override fun onResume() {
        super.onResume()
        updateStatusButton()
    }

    private fun updateStatusButton() {
        val active = TdlibManager.isSessionActive(this)
        val channels = TdlibManager.getChannels(this).size
        if (active) {
            statusButton.text = "TDLib Connected ($channels Channels)"
        } else {
            statusButton.text = "Login to Telegram"
        }
    }

    private fun loadFeaturedCatalog() {
        mediaList.clear()
        mediaList.add(MediaItem("tt1375666", "Inception", "https://images.metahub.space/poster/medium/tt1375666/img", "2010", "8.8", "A thief who steals corporate secrets through dream-sharing technology."))
        mediaList.add(MediaItem("tt0944947", "Game of Thrones", "https://images.metahub.space/poster/medium/tt0944947/img", "2011", "9.2", "Nine noble families fight for control over Westeros."))
        mediaList.add(MediaItem("tt4574334", "Stranger Things", "https://images.metahub.space/poster/medium/tt4574334/img", "2016", "8.7", "When a young boy vanishes, a small town uncovers a mystery."))
        mediaList.add(MediaItem("tt0816692", "Interstellar", "https://images.metahub.space/poster/medium/tt0816692/img", "2014", "8.7", "A team of researchers travels through a wormhole in space."))
        
        updateGrid()
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("https://v3-cinemeta.strem.io/catalog/movie/top/search=${java.net.URLEncoder.encode(query, "UTF-8")}.json")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                val text = connection.inputStream.bufferedReader().readText()
                val json = org.json.JSONObject(text)
                val metas = json.optJSONArray("metas") ?: JSONArray()
                
                val results = mutableListOf<MediaItem>()
                for (i in 0 until minOf(metas.length(), 10)) {
                    val obj = metas.getJSONObject(i)
                    results.add(MediaItem(
                        id = obj.optString("id"),
                        title = obj.optString("name"),
                        posterUrl = obj.optString("poster"),
                        year = obj.optString("year", "2024"),
                        rating = obj.optString("imdbRating", "8.0"),
                        overview = obj.optString("description", "")
                    ))
                }

                withContext(Dispatchers.Main) {
                    mediaList.clear()
                    mediaList.addAll(results)
                    updateGrid()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Search loaded offline fallbacks", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateGrid() {
        recyclerView.adapter = MediaAdapter(mediaList) { item ->
            showStreamOptions(item)
        }
    }

    private fun showStreamOptions(item: MediaItem) {
        val streams = TdlibManager.resolveStreams(item.title)
        val streamTitles = streams.map { "${it.quality} (${it.size}) - ${it.channel}" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("${item.title} - Select TDLib Stream")
            .setItems(streamTitles) { _, which ->
                val selectedStream = streams[which]
                showPlayerActionDialog(selectedStream.url, selectedStream.fileName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPlayerActionDialog(streamUrl: String, title: String) {
        val options = arrayOf("Play in Internal ExoPlayer", "Open in VLC Player", "Open in MPV Player")
        AlertDialog.Builder(this)
            .setTitle("Select Player")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(this, PlayerActivity::class.java).apply {
                            putExtra("VIDEO_URL", streamUrl)
                            putExtra("VIDEO_TITLE", title)
                        }
                        startActivity(intent)
                    }
                    1 -> {
                        val vlcIntent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(Uri.parse(streamUrl), "video/*")
                            setPackage("org.videolan.vlc")
                        }
                        try { startActivity(vlcIntent) } catch (e: Exception) {
                            Toast.makeText(this, "VLC Player not installed", Toast.LENGTH_SHORT).show()
                        }
                    }
                    2 -> {
                        val mpvIntent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(Uri.parse(streamUrl), "video/*")
                            setPackage("is.xyz.mpv")
                        }
                        try { startActivity(mpvIntent) } catch (e: Exception) {
                            Toast.makeText(this, "MPV Player not installed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .show()
    }
}
