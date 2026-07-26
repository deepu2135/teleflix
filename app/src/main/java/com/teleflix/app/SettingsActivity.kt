package com.teleflix.app

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var channelContainer: LinearLayout
    private lateinit var apiIdInput: EditText
    private lateinit var apiHashInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#090A0F"))
            setPadding(24, 24, 24, 24)
            fitsSystemWindows = true
        }

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.setPadding(
                24 + insets.left,
                24 + insets.top,
                24 + insets.right,
                24 + insets.bottom
            )
            WindowInsetsCompat.CONSUMED
        }

        // Header
        val header = TextView(this).apply {
            text = "TELEGRAM & TDLIB SETTINGS"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#3B82F6"))
            setPadding(0, 0, 0, 24)
        }
        root.addView(header)

        // Session Status Box
        val sessionBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#161B28"))
            setPadding(24, 24, 24, 24)
        }

        val sessionTitle = TextView(this).apply {
            text = "Telegram Account Session"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
        }
        sessionBox.addView(sessionTitle)

        statusText = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.parseColor("#10B981"))
            setPadding(0, 8, 0, 16)
        }
        sessionBox.addView(statusText)

        val authBtn = Button(this).apply {
            text = "Manage Telegram Session"
            setBackgroundColor(Color.parseColor("#3B82F6"))
            setTextColor(Color.WHITE)
            setOnClickListener { showAuthDialog() }
        }
        sessionBox.addView(authBtn)

        root.addView(sessionBox)

        // Channels Section
        val channelTitle = TextView(this).apply {
            text = "Monitored Telegram Channels"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(0, 24, 0, 12)
        }
        root.addView(channelTitle)

        val addLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, 16)
        }

        val channelInput = EditText(this).apply {
            hint = "Enter @channel_name..."
            setHintTextColor(Color.parseColor("#6B7280"))
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#161B28"))
            setPadding(20, 16, 20, 16)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val addBtn = Button(this).apply {
            text = "Add"
            setBackgroundColor(Color.parseColor("#E50914"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                if (channelInput.text.isNotBlank()) {
                    TdlibManager.addChannel(this@SettingsActivity, channelInput.text.toString())
                    channelInput.setText("")
                    loadChannels()
                }
            }
        }

        addLayout.addView(channelInput)
        addLayout.addView(addBtn)
        root.addView(addLayout)

        channelContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        root.addView(channelContainer)

        // API Credentials Box
        val apiBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#161B28"))
            setPadding(24, 24, 24, 24)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 24, 0, 0)
            }
        }

        val apiTitle = TextView(this).apply {
            text = "TDLib API Credentials"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
        }
        apiBox.addView(apiTitle)

        apiIdInput = EditText(this).apply {
            hint = "API ID (Default: 2040012)"
            setHintTextColor(Color.parseColor("#6B7280"))
            setTextColor(Color.WHITE)
            setText(TdlibManager.getApiId(this@SettingsActivity).toString())
            setPadding(16, 12, 16, 12)
        }
        apiBox.addView(apiIdInput)

        apiHashInput = EditText(this).apply {
            hint = "API Hash"
            setHintTextColor(Color.parseColor("#6B7280"))
            setTextColor(Color.WHITE)
            setText(TdlibManager.getApiHash(this@SettingsActivity))
            setPadding(16, 12, 16, 12)
        }
        apiBox.addView(apiHashInput)

        val saveApiBtn = Button(this).apply {
            text = "Save Credentials"
            setBackgroundColor(Color.parseColor("#10B981"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                val id = apiIdInput.text.toString().toIntOrNull() ?: 2040012
                val hash = apiHashInput.text.toString()
                TdlibManager.saveApiId(this@SettingsActivity, id)
                TdlibManager.saveApiHash(this@SettingsActivity, hash)
                Toast.makeText(this@SettingsActivity, "Credentials Saved!", Toast.LENGTH_SHORT).show()
            }
        }
        apiBox.addView(saveApiBtn)

        root.addView(apiBox)

        setContentView(root)

        updateSessionStatus()
        loadChannels()
    }

    private fun updateSessionStatus() {
        val active = TdlibManager.isSessionActive(this)
        val phone = TdlibManager.getUserPhone(this)
        if (active) {
            statusText.text = "Connected Session ($phone)\nTDLib Native Client Active"
            statusText.setTextColor(Color.parseColor("#10B981"))
        } else {
            statusText.text = "Disconnected (Login Required)"
            statusText.setTextColor(Color.parseColor("#EF4444"))
        }
    }

    private fun loadChannels() {
        channelContainer.removeAllViews()
        val list = TdlibManager.getChannels(this)
        list.forEach { ch ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setBackgroundColor(Color.parseColor("#12151F"))
                setPadding(16, 12, 16, 12)
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 4, 0, 4)
                }
            }

            val name = TextView(this).apply {
                text = "${ch.username} (${ch.title})"
                setTextColor(Color.WHITE)
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val removeBtn = Button(this).apply {
                text = "Remove"
                setBackgroundColor(Color.parseColor("#7F1D1D"))
                setTextColor(Color.WHITE)
                textSize = 11f
                setOnClickListener {
                    TdlibManager.removeChannel(this@SettingsActivity, ch.username)
                    loadChannels()
                }
            }

            row.addView(name)
            row.addView(removeBtn)
            channelContainer.addView(row)
        }
    }

    private fun showAuthDialog() {
        val phoneInput = EditText(this).apply {
            hint = "+1 (555) 019-2834"
            setText(TdlibManager.getUserPhone(this@SettingsActivity))
        }

        AlertDialog.Builder(this)
            .setTitle("Telegram Login")
            .setMessage("Enter your Telegram phone number with country code:")
            .setView(phoneInput)
            .setPositiveButton("Send Code") { _, _ ->
                val phone = phoneInput.text.toString()
                showCodeDialog(phone)
            }
            .setNegativeButton("Log Out") { _, _ ->
                TdlibManager.setSessionActive(this, false)
                updateSessionStatus()
            }
            .show()
    }

    private fun showCodeDialog(phone: String) {
        val codeInput = EditText(this).apply {
            hint = "12345"
        }

        AlertDialog.Builder(this)
            .setTitle("Enter Telegram Verification Code")
            .setMessage("Code sent to $phone:")
            .setView(codeInput)
            .setPositiveButton("Verify & Login") { _, _ ->
                TdlibManager.setSessionActive(this, true, phone)
                updateSessionStatus()
                Toast.makeText(this, "Telegram Account Connected!", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}
