package com.umc.hellodoctor.feature.drug.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.hellodoctor.databinding.FragmentDrugAlarmDetailBinding
import com.umc.hellodoctor.feature.drug.presentation.adapter.AlarmTimeAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class DrugAlarmDetailFragment : Fragment() {
    private val viewModel: DrugPlanViewModel by viewModels()
    private lateinit var binding: FragmentDrugAlarmDetailBinding
    private val args: DrugAlarmDetailFragmentArgs by navArgs()

    private lateinit var alarmAdapter: AlarmTimeAdapter
    private lateinit var medicineAdapter: MedicineAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDrugAlarmDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val planId = args.planId

        setupRecyclerViews()
        setupButtons()
        observeData()

        viewModel.loadPlan(planId)
    }

    private fun setupRecyclerViews() {
        alarmAdapter = AlarmTimeAdapter()
        binding.rvAlarmTimes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = alarmAdapter
        }

        medicineAdapter = MedicineAdapter()
        binding.rvMedicines.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = medicineAdapter
        }
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnEdit.setOnClickListener {
            viewModel.selectedPlan.value?.let { plan ->
                val action =
                    DrugAlarmDetailFragmentDirections.actionDrugAlarmDetailFragmentToDrugInfoInputFragment(
                        plan.medicines.toTypedArray(),
                        plan.id,
                    )
                findNavController().navigate(action)
            }
        }
    }

    private fun observeData() {
        viewModel.selectedPlan.observe(viewLifecycleOwner) { plan ->
            plan?.let {
                val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)

                binding.apply {
                    tvPharmacyName.text = it.pharmacyName
                    tvIntakeType.text =
                        when (it.intakeType.name) {
                            "PRE_MEAL" -> "식전"
                            "BETWEEN_MEALS" -> "식간"
                            "POST_MEAL" -> "식후"
                            else -> "기타"
                        }
                    tvStartDate.text = dateFormat.format(Date(it.startDateMillis))
                    tvEndDate.text = dateFormat.format(Date(it.endDateMillis))

                    tvAlarmTitle.text = "복용 시간 (${it.alarms.size}회)"
                    tvMedicineTitle.text = "약 목록 (${it.medicines.size}개)"
                }

                alarmAdapter.submitList(it.alarms)
                medicineAdapter.submitList(it.medicines)
            }
        }
    }
}
