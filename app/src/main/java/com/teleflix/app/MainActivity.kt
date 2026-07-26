package com.teleflix.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
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
import org.json.JSONObject
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
    private lateinit var categoryLabel: TextView
    private lateinit var loadingText: TextView

    private val mediaList = mutableListOf<MediaItem>()
    private var selectedCategory = "movie/top"

    private val categories = listOf(
        "Top Movies" to "movie/top",
        "Top Series" to "series/top",
        "New Movies" to "movie/year",
        "New Series" to "series/year",
        "IMDB Top" to "movie/imdbRating"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.parseColor("#090A0F"))
            setPadding(16, 16, 16, 16)
        }

        // Header Bar
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, 12)
        }

        val titleView = TextView(this).apply {
            text = "TELEFLIX"
            textSize = 22f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#E50914"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        statusButton = Button(this).apply {
            text = "TDLib Settings"
            textSize = 10f
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
        val searchLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, 12)
        }

        searchInput = EditText(this).apply {
            hint = "Search movies, series..."
            setHintTextColor(android.graphics.Color.parseColor("#6B7280"))
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(android.graphics.Color.parseColor("#161B28"))
            setPadding(24, 14, 24, 14)
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        searchButton = Button(this).apply {
            text = "🔍"
            textSize = 16f
            setBackgroundColor(android.graphics.Color.parseColor("#E50914"))
            setTextColor(android.graphics.Color.WHITE)
            setOnClickListener {
                val q = searchInput.text.toString()
                if (q.isNotBlank()) {
                    performSearch(q)
                }
            }
        }

        searchLayout.addView(searchInput)
        searchLayout.addView(searchButton)
        rootView.addView(searchLayout)

        // Category Tabs (Horizontal Scroll)
        val tabScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            setPadding(0, 0, 0, 12)
        }

        val tabRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        categories.forEach { (label, catalogId) ->
            val tab = Button(this).apply {
                text = label
                textSize = 11f
                setTextColor(android.graphics.Color.WHITE)
                val isSelected = catalogId == selectedCategory
                setBackgroundColor(
                    if (isSelected) android.graphics.Color.parseColor("#E50914")
                    else android.graphics.Color.parseColor("#1F2937")
                )
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 8, 0) }
                layoutParams = lp
                setOnClickListener {
                    selectedCategory = catalogId
                    categoryLabel.text = label
                    loadCinemeta(catalogId, label)
                    // Rebuild tab colors
                    for (i in 0 until tabRow.childCount) {
                        val child = tabRow.getChildAt(i) as Button
                        val cat = categories[i].second
                        child.setBackgroundColor(
                            if (cat == selectedCategory) android.graphics.Color.parseColor("#E50914")
                            else android.graphics.Color.parseColor("#1F2937")
                        )
                    }
                }
            }
            tabRow.addView(tab)
        }

        tabScroll.addView(tabRow)
        rootView.addView(tabScroll)

        // Category Label
        categoryLabel = TextView(this).apply {
            text = "Top Movies"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.WHITE)
            setPadding(0, 0, 0, 8)
        }
        rootView.addView(categoryLabel)

        // Loading indicator
        loadingText = TextView(this).apply {
            text = "Loading from Cinemeta..."
            textSize = 13f
            setTextColor(android.graphics.Color.parseColor("#6B7280"))
            setPadding(0, 0, 0, 8)
        }
        rootView.addView(loadingText)

        // Media Grid
        recyclerView = RecyclerView(this).apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 1f
            )
        }
        rootView.addView(recyclerView)

        setContentView(rootView)

        // Load catalogue from Cinemeta API
        loadCinemeta("movie/top", "Top Movies")
    }

    override fun onResume() {
        super.onResume()
        updateStatusButton()
    }

    private fun updateStatusButton() {
        val active = TdlibManager.isSessionActive(this)
        val channels = TdlibManager.getChannels(this).size
        if (active) {
            statusButton.text = "TDLib ($channels ch)"
        } else {
            statusButton.text = "Login"
        }
    }

    private fun loadCinemeta(catalogId: String, label: String) {
        loadingText.text = "Loading $label from Cinemeta..."
        loadingText.visibility = android.view.View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("https://v3-cinemeta.strem.io/catalog/$catalogId.json")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 8000
                connection.readTimeout = 8000
                val text = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(text)
                val metas = json.optJSONArray("metas") ?: JSONArray()

                val results = parseMetas(metas, 20)

                withContext(Dispatchers.Main) {
                    mediaList.clear()
                    mediaList.addAll(results)
                    loadingText.visibility = android.view.View.GONE
                    updateGrid()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingText.text = "Failed to load. Showing fallback."
                    loadFallbackCatalog()
                }
            }
        }
    }

    private fun performSearch(query: String) {
        categoryLabel.text = "Search: \"$query\""
        loadingText.text = "Searching Cinemeta..."
        loadingText.visibility = android.view.View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            val allResults = mutableListOf<MediaItem>()

            // Search both movies and series
            for (type in listOf("movie", "series")) {
                try {
                    val url = URL("https://v3-cinemeta.strem.io/catalog/$type/top/search=${java.net.URLEncoder.encode(query, "UTF-8")}.json")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 8000
                    connection.readTimeout = 8000
                    val text = connection.inputStream.bufferedReader().readText()
                    val json = JSONObject(text)
                    val metas = json.optJSONArray("metas") ?: JSONArray()
                    allResults.addAll(parseMetas(metas, 10))
                } catch (_: Exception) { }
            }

            withContext(Dispatchers.Main) {
                mediaList.clear()
                if (allResults.isNotEmpty()) {
                    mediaList.addAll(allResults)
                    loadingText.visibility = android.view.View.GONE
                } else {
                    loadingText.text = "No results found for \"$query\""
                }
                updateGrid()
            }
        }
    }

    private fun parseMetas(metas: JSONArray, limit: Int): List<MediaItem> {
        val results = mutableListOf<MediaItem>()
        for (i in 0 until minOf(metas.length(), limit)) {
            val obj = metas.getJSONObject(i)
            results.add(MediaItem(
                id = obj.optString("id"),
                title = obj.optString("name"),
                posterUrl = obj.optString("poster"),
                year = obj.optString("releaseInfo", obj.optString("year", "")),
                rating = obj.optString("imdbRating", "—"),
                overview = obj.optString("description", "")
            ))
        }
        return results
    }

    private fun loadFallbackCatalog() {
        mediaList.clear()
        mediaList.add(MediaItem("tt1375666", "Inception", "https://images.metahub.space/poster/medium/tt1375666/img", "2010", "8.8", "A thief who steals corporate secrets through dream-sharing technology."))
        mediaList.add(MediaItem("tt0944947", "Game of Thrones", "https://images.metahub.space/poster/medium/tt0944947/img", "2011", "9.2", "Nine noble families fight for control over Westeros."))
        mediaList.add(MediaItem("tt4574334", "Stranger Things", "https://images.metahub.space/poster/medium/tt4574334/img", "2016", "8.7", "When a young boy vanishes, a small town uncovers a mystery."))
        mediaList.add(MediaItem("tt0816692", "Interstellar", "https://images.metahub.space/poster/medium/tt0816692/img", "2014", "8.7", "A team of researchers travels through a wormhole in space."))
        updateGrid()
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
            .setTitle("${item.title} - Select Stream")
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
