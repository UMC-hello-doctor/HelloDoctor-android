package com.umc.hellodoctor.feature.drug.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.hellodoctor.databinding.ItemAlarmTimeBinding
import com.umc.hellodoctor.feature.drug.data.database.AlarmInfo

class AlarmTimeAdapter :
    ListAdapter<AlarmInfo, AlarmTimeAdapter.ViewHolder>(AlarmTimeDiffCallback()) {
    private var isEditMode: Boolean = false
    private var onDeleteClick: ((Int) -> Unit)? = null

    inner class ViewHolder(private val binding: ItemAlarmTimeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(alarm: AlarmInfo) {
            val timeText = String.format(java.util.Locale.US, "%02d:%02d", alarm.hour, alarm.minute)
            binding.tvAlarmTime.text = timeText

            // 수정 모드에만 삭제 버튼 표시
            binding.btnDelete.visibility =
                if (isEditMode) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }

            binding.btnDelete.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick?.invoke(position)
                }
            }
        }
    }

    fun setEditMode(isEdit: Boolean) {
        isEditMode = isEdit
        notifyDataSetChanged()
    }

    fun setOnDeleteClickListener(callback: (Int) -> Unit) {
        onDeleteClick = callback
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        return ViewHolder(
            ItemAlarmTimeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    class AlarmTimeDiffCallback : DiffUtil.ItemCallback<AlarmInfo>() {
        override fun areItemsTheSame(
            oldItem: AlarmInfo,
            newItem: AlarmInfo,
        ): Boolean {
            return oldItem.alarmId == newItem.alarmId
        }

        override fun areContentsTheSame(
            oldItem: AlarmInfo,
            newItem: AlarmInfo,
        ): Boolean {
            return oldItem == newItem
        }
    }
}
