package com.teleflix.app

import android.graphics.Color
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MediaAdapter(
    private val items: List<MediaItem>,
    private val onClick: (MediaItem) -> Unit
) : RecyclerView.Adapter<MediaAdapter.ViewHolder>() {

    class ViewHolder(val layout: LinearLayout, val titleText: TextView, val yearText: TextView, val overviewText: TextView) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#161B28"))
            setPadding(24, 24, 24, 24)
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 8, 8, 8)
            }
        }

        val titleView = TextView(context).apply {
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            maxLines = 2
        }

        val yearView = TextView(context).apply {
            textSize = 12f
            setTextColor(Color.parseColor("#F59E0B"))
            setPadding(0, 6, 0, 4)
        }

        val overviewView = TextView(context).apply {
            textSize = 11f
            setTextColor(Color.parseColor("#9CA3AF"))
            maxLines = 3
        }

        card.addView(titleView)
        card.addView(yearView)
        card.addView(overviewView)

        return ViewHolder(card, titleView, yearView, overviewView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.titleText.text = item.title
        holder.yearText.text = "${item.year} • ⭐ IMDb ${item.rating}"
        holder.overviewText.text = item.overview
        holder.layout.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size
}
