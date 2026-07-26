package com.teleflix.app

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class MediaAdapter(
    private val items: List<MediaItem>,
    private val onClick: (MediaItem) -> Unit
) : RecyclerView.Adapter<MediaAdapter.ViewHolder>() {

    class ViewHolder(
        val layout: LinearLayout,
        val posterView: ImageView,
        val titleText: TextView,
        val yearText: TextView,
        val overviewText: TextView
    ) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context

        // Convert dp to pixels for responsive dimensions
        fun dpToPx(dp: Int): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                context.resources.displayMetrics
            ).toInt()
        }

        // Card Container with Rounded Background
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val shape = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(Color.parseColor("#161B28"))
                cornerRadius = dpToPx(12).toFloat()
            }
            background = shape
            setPadding(0, 0, 0, dpToPx(12))
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(12))
            }
        }

        // Movie Poster Thumbnail (Aspect ratio ~2:3, height 240dp)
        val posterView = ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(240)
            )
            setBackgroundColor(Color.parseColor("#1F2937")) // dark loading placeholder block
        }

        // Text Content Container
        val textContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(10), dpToPx(8), dpToPx(10), 0)
        }

        val titleView = TextView(context).apply {
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }

        val yearView = TextView(context).apply {
            textSize = 11f
            setTextColor(Color.parseColor("#F59E0B"))
            setPadding(0, dpToPx(2), 0, dpToPx(4))
        }

        val overviewView = TextView(context).apply {
            textSize = 10f
            setTextColor(Color.parseColor("#9CA3AF"))
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
        }

        textContainer.addView(titleView)
        textContainer.addView(yearView)
        textContainer.addView(overviewView)

        card.addView(posterView)
        card.addView(textContainer)

        return ViewHolder(card, posterView, titleView, yearView, overviewView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.titleText.text = item.title
        holder.yearText.text = "${item.year} • ⭐ ${item.rating}"
        holder.overviewText.text = item.overview

        // Load movie/series poster with Glide
        if (item.posterUrl.isNotBlank()) {
            Glide.with(holder.itemView.context)
                .load(item.posterUrl)
                .transform(RoundedCorners(16))
                .error(android.R.drawable.ic_dialog_alert)
                .into(holder.posterView)
        } else {
            holder.posterView.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.layout.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size
}
