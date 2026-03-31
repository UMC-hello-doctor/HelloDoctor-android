package com.umc.hellodoctor.feature.drug.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.hellodoctor.databinding.ItemDrugAlarmBinding
import com.umc.hellodoctor.feature.drug.data.database.DrugPlanEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DrugAlarmAdapter(
    private val onItemClick: (DrugPlanEntity) -> Unit,
) : ListAdapter<DrugPlanEntity, DrugAlarmAdapter.ViewHolder>(DrugPlanDiffCallback()) {
    inner class ViewHolder(private val binding: ItemDrugAlarmBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(plan: DrugPlanEntity) {
            val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.KOREA)
            val startDate = dateFormat.format(Date(plan.startDateMillis))
            val endDate = dateFormat.format(Date(plan.endDateMillis))

            binding.apply {
                tvStartDate.text = startDate
                tvPharmacyName.text = plan.pharmacyName
                tvMedicineInfo.text = "${plan.medicines.size}원 ${plan.alarms.size}회"
                tvExpireDate.text = "EXP: $endDate"

                root.setOnClickListener {
                    onItemClick(plan)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        return ViewHolder(
            ItemDrugAlarmBinding.inflate(
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

    class DrugPlanDiffCallback : DiffUtil.ItemCallback<DrugPlanEntity>() {
        override fun areItemsTheSame(
            oldItem: DrugPlanEntity,
            newItem: DrugPlanEntity,
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: DrugPlanEntity,
            newItem: DrugPlanEntity,
        ): Boolean {
            return oldItem == newItem
        }
    }
}
