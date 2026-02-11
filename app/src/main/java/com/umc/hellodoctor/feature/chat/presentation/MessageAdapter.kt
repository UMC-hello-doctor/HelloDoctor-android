package com.umc.hellodoctor.feature.chat.presentation

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.umc.hellodoctor.R
import com.umc.hellodoctor.databinding.ViewChatHistoryAiBinding
import com.umc.hellodoctor.databinding.ViewChatHistoryHumanBinding
import com.umc.hellodoctor.feature.chat.domain.model.Message
import com.umc.hellodoctor.feature.chat.domain.model.MessageType

class MessageAdapter(private val messages: MutableList<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_BOT = 0
        private const val VIEW_TYPE_USER = 1
    }

    // ViewHolder - AI(봇)
    class BotViewHolder(val binding: ViewChatHistoryAiBinding) :
        RecyclerView.ViewHolder(binding.root)

    // ViewHolder - Human(사용자)
    class UserViewHolder(val binding: ViewChatHistoryHumanBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isBot) VIEW_TYPE_BOT else VIEW_TYPE_USER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_BOT -> {
                val binding = ViewChatHistoryAiBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                BotViewHolder(binding)
            }
            VIEW_TYPE_USER -> {
                val binding = ViewChatHistoryHumanBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                UserViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        when (holder) {
            is BotViewHolder -> {
                holder.binding.text.text = message.text
                // 메시지 타입에 따라 스타일 적용
                applyMessageTypeStyle(holder.binding.text, message)
            }
            is UserViewHolder -> {
                holder.binding.text.text = message.text
            }
        }
    }

    /**
     * 메시지 타입에 따라 스타일 적용
     */
    private fun applyMessageTypeStyle(textView: TextView, message: Message) {
        // 기본 스타일 초기화
        textView.setBackgroundColor(Color.TRANSPARENT)
        textView.setPadding(16, 16, 16, 16)
        textView.setTypeface(null, Typeface.NORMAL)

        when (message.type) {
            MessageType.QUESTION -> {
                // 질문 스타일
                textView.setTextColor(Color.WHITE)
            }
            MessageType.ANSWER -> {
                // 답변 스타일
                textView.setTextColor(Color.WHITE)
            }
            MessageType.RECOMMENDATION -> {
                // 추천 진료과 스타일 (파란색 강조)
                textView.setTextColor(0xFF0066CC.toInt())
                textView.textSize = 14f
                textView.setTypeface(null, Typeface.BOLD)
            }
            MessageType.SYMPTOM_SUMMARY -> {
                // 증상 요약 스타일 (주황색 강조)
                textView.setTextColor(0xFFFF6600.toInt())
                textView.textSize = 14f
            }
            MessageType.EMERGENCY -> {
                // ⚠️ 응급 메시지 스타일 (붉은색 텍스트만)
                textView.setBackgroundColor(Color.TRANSPARENT) // 배경 투명
                textView.setTextColor(0xFFFF4444.toInt()) // 밝은 붉은색 텍스트
                textView.textSize = 15f
                textView.setTypeface(null, Typeface.BOLD)
                textView.setPadding(16, 16, 16, 16)
            }
        }

        // isEmergency 플래그가 true면 응급 스타일 강제 적용
        if (message.isEmergency) {
            textView.setBackgroundColor(Color.TRANSPARENT) // 배경 투명
            textView.setTextColor(0xFFFF4444.toInt()) // 밝은 붉은색 텍스트
            textView.textSize = 15f
            textView.setTypeface(null, Typeface.BOLD)
            textView.setPadding(16, 16, 16, 16)
        }
    }

    override fun getItemCount() = messages.size

    fun addMessage(msg: Message) {
        messages.add(msg)
        notifyItemInserted(messages.size - 1)
    }

    fun addMessages(newMessages: List<Message>) {
        val startPosition = messages.size
        messages.addAll(newMessages)
        notifyItemRangeInserted(startPosition, newMessages.size)
    }

    fun clearMessages() {
        messages.clear()
        notifyDataSetChanged()
    }
}

