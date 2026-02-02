package com.umc.hellodoctor.feature.navermap.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.hellodoctor.databinding.ItemHospitalBinding
import com.umc.hellodoctor.feature.navermap.data.HospitalItem

class HospitalAdapter(
    private val onCallClick: (String) -> Unit,
    private val onMarkerClick: (Double, Double) -> Unit
) : ListAdapter<HospitalItem, HospitalAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(val binding: ItemHospitalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(hospital: HospitalItem) {
            binding.hospitalName.text = hospital.name
            binding.hospitalAddress.text = hospital.address
            binding.hospitalTel.text = hospital.tel
            binding.hospitalDistance.text = hospital.distance.toInt().toString() + "m"
            binding.hospitalHours.text = hospital.businessHours

            // call 버튼 – 전화 다이얼러 열기
            binding.call.setOnClickListener {
                onCallClick(hospital.tel)
            }

            // marker 버튼 – 지도에 카메라 이동
            binding.marker.setOnClickListener {
                onMarkerClick(hospital.latitude, hospital.longitude)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHospitalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<HospitalItem>() {
            override fun areItemsTheSame(oldItem: HospitalItem, newItem: HospitalItem): Boolean =
                oldItem.tel == newItem.tel

            override fun areContentsTheSame(oldItem: HospitalItem, newItem: HospitalItem): Boolean =
                oldItem == newItem
        }
    }
}