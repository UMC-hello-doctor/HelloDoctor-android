package com.umc.hellodoctor.feature.drug.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umc.hellodoctor.databinding.ItemAlarmInfoBinding
import com.umc.hellodoctor.feature.drug.data.database.AlarmInfo

class AlarmInfoAdapter(
    private val alarms: List<AlarmInfo>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<AlarmInfoAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemAlarmInfoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(alarm: AlarmInfo) {
            val timeText = String.format("%02d:%02d", alarm.hour, alarm.minute)
            binding.tvAlarmTime.text = timeText

            binding.btnDelete.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onDeleteClick(pos)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAlarmInfoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(alarms[position])
    }

    override fun getItemCount() = alarms.size
}
