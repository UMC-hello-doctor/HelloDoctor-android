package com.umc.hellodoctor.feature.drug.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.umc.hellodoctor.R
import com.umc.hellodoctor.core.util.StartCropTransformation
import com.umc.hellodoctor.databinding.ItemSelectedMedicineBinding
import com.umc.hellodoctor.feature.drug.data.model.MedicineSearchItem

class SelectedMedicineAdapter(
    private val onDeleteClick: (MedicineSearchItem) -> Unit,
) : ListAdapter<MedicineSearchItem, SelectedMedicineAdapter.ViewHolder>(SelectedMedicineDiffCallback()) {
    inner class ViewHolder(
        private val binding: ItemSelectedMedicineBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MedicineSearchItem) {
            binding.tvMedicineName.text = item.medicineName
            binding.tvEntpName.text = item.entpName
            binding.tvEfficacy.text = item.efficacy

            Glide.with(binding.ivMedicineImage.context)
                .load(item.medicineImage)
                .placeholder(R.drawable.ic_medicine)
                .error(R.drawable.ic_medicine)
                .skipMemoryCache(false)
                .transform(StartCropTransformation())
                .into(binding.ivMedicineImage)

            binding.btnDelete.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val binding =
            ItemSelectedMedicineBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    private class SelectedMedicineDiffCallback : DiffUtil.ItemCallback<MedicineSearchItem>() {
        override fun areItemsTheSame(
            oldItem: MedicineSearchItem,
            newItem: MedicineSearchItem,
        ): Boolean {
            return oldItem.medicineId == newItem.medicineId
        }

        override fun areContentsTheSame(
            oldItem: MedicineSearchItem,
            newItem: MedicineSearchItem,
        ): Boolean {
            return oldItem == newItem
        }
    }
}
