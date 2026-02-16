package com.umc.hellodoctor.feature.drug

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.umc.hellodoctor.databinding.ItemDrugResultBinding
import com.umc.hellodoctor.feature.drug.data.model.MedicineSearchItem

class DrugSearchAdapter : ListAdapter<MedicineSearchItem, DrugSearchAdapter.DrugSearchViewHolder>(
    DrugSearchDiffCallback()
) {

    class DrugSearchViewHolder(
        private val binding: ItemDrugResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MedicineSearchItem) {
            binding.tvDrugName.text = item.medicineName
            binding.tvDrugDose.text = item.entpName

            // Glide를 사용하여 이미지 로딩 (왼쪽 기준 crop)
            Glide.with(binding.ivDrugImage.context)
                .load(item.medicineImage)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .skipMemoryCache(false)
                .transform(StartCropTransformation())
                .into(binding.ivDrugImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrugSearchViewHolder {
        val binding = ItemDrugResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DrugSearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DrugSearchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class DrugSearchDiffCallback : DiffUtil.ItemCallback<MedicineSearchItem>() {
        override fun areItemsTheSame(oldItem: MedicineSearchItem, newItem: MedicineSearchItem): Boolean {
            return oldItem.medicineId == newItem.medicineId
        }

        override fun areContentsTheSame(oldItem: MedicineSearchItem, newItem: MedicineSearchItem): Boolean {
            return oldItem == newItem
        }
    }
}

