package com.umc.hellodoctor.feature.auth.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umc.hellodoctor.core.util.makeHighlightedTitle
import com.umc.hellodoctor.databinding.ItemOnboardingPageBinding

class OnboardingAdapter(

    private val pages: List<OnboardingPage>,
    private val onStartClick: () -> Unit
) : RecyclerView.Adapter<OnboardingAdapter.VH>() {

    inner class VH(val binding: ItemOnboardingPageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemOnboardingPageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun getItemCount() = pages.size

    private val HIGHLIGHT_COLOR = "#1852FF"

    override fun onBindViewHolder(holder: VH, position: Int) {
        val page = pages[position]

        val isFirst = position == 0
        holder.binding.TitleCenter.visibility = if (isFirst) View.VISIBLE else View.GONE
        holder.binding.TitleLeft.visibility = if (!isFirst) View.VISIBLE else View.GONE

        val titleView = if (isFirst) holder.binding.TitleCenter else holder.binding.TitleLeft

        titleView.text = buildHighlightedText(
            fullText = page.title,
            highlight = page.highlight,
            colorHex = HIGHLIGHT_COLOR
        )

        holder.binding.Desc.visibility = if (!isFirst && page.desc.isNotBlank()) View.VISIBLE else View.GONE
        holder.binding.Desc.text = page.desc

        val hasTwo = page.image2 != null
        holder.binding.imgSingle.visibility = if (!hasTwo) View.VISIBLE else View.GONE
        holder.binding.groupDouble.visibility = if (hasTwo) View.VISIBLE else View.GONE

        if (!hasTwo) {
            holder.binding.imgSingle.setImageResource(page.image1)
        } else {
            holder.binding.imgLeft.setImageResource(page.image1)
            holder.binding.imgRight.setImageResource(page.image2!!)
        }

        holder.binding.btnStart.visibility = if (page.showStartButton) View.VISIBLE else View.GONE
        holder.binding.btnStart.setOnClickListener { onStartClick() }
    }

    private fun buildHighlightedText(fullText: String, highlight: String?, colorHex: String): CharSequence {
        if (highlight.isNullOrBlank()) return fullText

        val start = fullText.indexOf(highlight)
        if (start < 0) return fullText
        val end = start + highlight.length

        return android.text.SpannableString(fullText).apply {
            setSpan(
                android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor(colorHex)),
                start, end,
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }


}

