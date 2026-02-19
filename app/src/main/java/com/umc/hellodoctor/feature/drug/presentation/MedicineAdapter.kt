package com.umc.hellodoctor.feature.drug.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.hellodoctor.databinding.ItemMedicineBinding
import com.umc.hellodoctor.feature.drug.data.model.MedicineSearchItem

class MedicineAdapter :
    ListAdapter<MedicineSearchItem, MedicineAdapter.ViewHolder>(MedicineDiffCallback()) {

    inner class ViewHolder(private val binding: ItemMedicineBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(medicine: MedicineSearchItem) {
            binding.apply {
                tvMedicineName.text = medicine.medicineName
                tvEntpName.text = medicine.entpName
                tvEfficacy.text = medicine.efficacy
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemMedicineBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MedicineDiffCallback : DiffUtil.ItemCallback<MedicineSearchItem>() {
        override fun areItemsTheSame(
            oldItem: MedicineSearchItem,
            newItem: MedicineSearchItem
        ): Boolean {
            return oldItem.medicineName == newItem.medicineName
        }

        override fun areContentsTheSame(
            oldItem: MedicineSearchItem,
            newItem: MedicineSearchItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}

