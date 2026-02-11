package com.umc.hellodoctor.feature.chat.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.hellodoctor.databinding.ItemHistoryBinding
import com.umc.hellodoctor.feature.chat.domain.model.ChatSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onItemClick: (ChatSession) -> Unit
) : ListAdapter<ChatSession, HistoryAdapter.HistoryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HistoryViewHolder(
        private val binding: ItemHistoryBinding,
        private val onItemClick: (ChatSession) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(session: ChatSession) {
            val department = session.recommendedDepartments.firstOrNull() ?: "진료과 없음"
            val preview = session.answers.firstOrNull() ?: "답변 없음"
            val timestamp = formatTimestamp(session.createdAt)

            binding.historyTitle.text = department
            binding.historyPreview.text = preview
            binding.historyTimestamp.text = timestamp

            binding.root.setOnClickListener { onItemClick(session) }
        }

        private fun formatTimestamp(createdAt: Long): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            return formatter.format(Date(createdAt))
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ChatSession>() {
        override fun areItemsTheSame(oldItem: ChatSession, newItem: ChatSession): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatSession, newItem: ChatSession): Boolean {
            return oldItem == newItem
        }
    }
}

