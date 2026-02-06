package com.umc.hellodoctor.feature.navermap.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.hellodoctor.databinding.ItemHospitalBinding
import com.umc.hellodoctor.feature.navermap.data.HospitalItem

class HospitalAdapter : ListAdapter<HospitalItem, HospitalAdapter.HospitalViewHolder>(HospitalDiffCallback()) {

    class HospitalViewHolder(private val binding: ItemHospitalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HospitalItem) {
            binding.hospitalName.text = item.name
            binding.hospitalAddress.text = item.address
            binding.hospitalTel.text = item.tel
            binding.hospitalDistance.text = "${String.format("%.0f", item.distance)}m"
            binding.hospitalHours.text = item.businessHours
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalViewHolder {
        val binding = ItemHospitalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HospitalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HospitalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HospitalDiffCallback : DiffUtil.ItemCallback<HospitalItem>() {
        override fun areItemsTheSame(oldItem: HospitalItem, newItem: HospitalItem): Boolean {
            return oldItem.id == newItem.id  // 고유 ID 비교
        }

        override fun areContentsTheSame(oldItem: HospitalItem, newItem: HospitalItem): Boolean {
            return oldItem == newItem  // 내용 동일 여부
        }
    }
}
