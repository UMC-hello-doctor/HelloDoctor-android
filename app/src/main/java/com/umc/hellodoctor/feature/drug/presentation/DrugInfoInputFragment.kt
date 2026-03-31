package com.umc.hellodoctor.feature.drug.presentation

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.hellodoctor.core.util.toast
import com.umc.hellodoctor.databinding.FragmentDrugInfoInputBinding
import com.umc.hellodoctor.feature.drug.data.database.AlarmInfo
import com.umc.hellodoctor.feature.drug.data.database.DrugPlanEntity
import com.umc.hellodoctor.feature.drug.data.database.IntakeType
import com.umc.hellodoctor.feature.drug.data.model.MedicineSearchItem
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

@AndroidEntryPoint
class DrugInfoInputFragment : Fragment() {
    private var _binding: FragmentDrugInfoInputBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DrugPlanViewModel by viewModels()
    private val args: DrugInfoInputFragmentArgs by navArgs()

    private lateinit var alarmAdapter: AlarmInfoAdapter
    private val alarms = mutableListOf<AlarmInfo>()
    private var medicines = listOf<MedicineSearchItem>()

    private var selectedIntakeType = IntakeType.POST_MEAL
    private var startDate = Calendar.getInstance()
    private var endDate = Calendar.getInstance()

    private var isEditMode = false
    private var editingPlanId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDrugInfoInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        medicines = args.medicines.toList()
        editingPlanId = args.planId

        if (editingPlanId != null) {
            isEditMode = true
            loadExistingPlan(editingPlanId!!)
        }
        setupUI()
        setupRecyclerView()
        setupButtons()
    }

    private fun loadExistingPlan(planId: String) {
        viewModel.selectedPlan.observe(viewLifecycleOwner) { plan ->
            plan?.let {
                // 기존 데이터로 UI 채우기
                binding.etPharmacyName.setText(it.pharmacyName)

                // 복용 타입 설정
                val intakeTypeIndex =
                    when (it.intakeType) {
                        IntakeType.PRE_MEAL -> INTAKE_PRE_MEAL_INDEX
                        IntakeType.BETWEEN_MEALS -> INTAKE_BETWEEN_MEALS_INDEX
                        IntakeType.POST_MEAL -> INTAKE_POST_MEAL_INDEX
                        else -> INTAKE_OTHER_INDEX
                    }
                binding.spinnerIntakeType.setSelection(intakeTypeIndex)
                selectedIntakeType = it.intakeType

                // 날짜 설정
                startDate.timeInMillis = it.startDateMillis
                endDate.timeInMillis = it.endDateMillis
                updateDateDisplay()

                // 알람 시간 설정
                alarms.clear()
                alarms.addAll(it.alarms)
                alarmAdapter.notifyDataSetChanged()

                // 약 정보 설정
                medicines = it.medicines

                // 버튼 텍스트 변경
                binding.btnSave.text = "수정"
            }
        }
        viewModel.loadPlan(planId)
    }

    private fun setupUI() {
        // 복용 타입 스피너 설정
        val intakeTypes = arrayOf("식전", "식간", "식후", "기타")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, intakeTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerIntakeType.adapter = adapter
        binding.spinnerIntakeType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    selectedIntakeType =
                        when (position) {
                            0 -> IntakeType.PRE_MEAL
                            1 -> IntakeType.BETWEEN_MEALS
                            2 -> IntakeType.POST_MEAL
                            else -> IntakeType.OTHER
                        }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }

        // 날짜 초기값 설정
        updateDateDisplay()
    }

    private fun setupRecyclerView() {
        alarmAdapter =
            AlarmInfoAdapter(alarms) { position ->
                alarms.removeAt(position)
                alarmAdapter.notifyItemRemoved(position)
            }
        binding.rvAlarmTimes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = alarmAdapter
        }
    }

    private fun setupButtons() {
        binding.btnStartDate.setOnClickListener {
            showDatePickerDialog(startDate) { selectedDate ->
                startDate = selectedDate
                updateDateDisplay()
            }
        }

        binding.btnEndDate.setOnClickListener {
            showDatePickerDialog(endDate) { selectedDate ->
                endDate = selectedDate
                updateDateDisplay()
            }
        }

        binding.btnAddAlarmTime.setOnClickListener {
            showTimePickerDialog { hour, minute ->
                val alarmId = (alarms.maxOfOrNull { it.alarmId } ?: -1) + 1
                alarms.add(AlarmInfo(alarmId, hour, minute))
                alarmAdapter.notifyItemInserted(alarms.size - 1)
            }
        }

        binding.btnSave.setOnClickListener {
            saveDrugPlan()
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.KOREA)
        binding.tvStartDateDisplay.text = dateFormat.format(startDate.time)
        binding.tvEndDateDisplay.text = dateFormat.format(endDate.time)
    }

    private fun showDatePickerDialog(
        calendar: Calendar,
        onDateSelected: (Calendar) -> Unit,
    ) {
        val datePickerDialog =
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val newCalendar =
                        Calendar.getInstance().apply {
                            set(year, month, dayOfMonth)
                        }
                    onDateSelected(newCalendar)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
            )
        datePickerDialog.show()
    }

    private fun showTimePickerDialog(onTimeSelected: (Int, Int) -> Unit) {
        val timePickerDialog =
            TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    onTimeSelected(hourOfDay, minute)
                },
                DEFAULT_HOUR,
                DEFAULT_MINUTE,
                true,
            )
        timePickerDialog.show()
    }

    private fun saveDrugPlan() {
        val pharmacyName = binding.etPharmacyName.text.toString().trim()

        if (pharmacyName.isEmpty()) {
            Toast.makeText(requireContext(), "약국명을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        if (alarms.isEmpty()) {
            Toast.makeText(requireContext(), "복용 시간을 최소 1개 추가해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val planId = if (isEditMode) editingPlanId!! else UUID.randomUUID().toString()

        val plan =
            DrugPlanEntity(
                id = planId,
                pharmacyName = pharmacyName,
                intakeType = selectedIntakeType,
                startDateMillis = startDate.timeInMillis,
                endDateMillis = endDate.timeInMillis,
                alarms = alarms.toList(),
                medicines = medicines,
            )

        if (isEditMode) {
            viewModel.updatePlan(plan)
            toast("복약 계획이 수정되었습니다")
        } else {
            viewModel.savePlan(plan)
            toast("복약 계획이 저장되었습니다")
        }

        // 저장 후 복약 목록 화면으로 이동
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val INTAKE_PRE_MEAL_INDEX = 0
        private const val INTAKE_BETWEEN_MEALS_INDEX = 1
        private const val INTAKE_POST_MEAL_INDEX = 2
        private const val INTAKE_OTHER_INDEX = 3
        private const val DEFAULT_HOUR = 12
        private const val DEFAULT_MINUTE = 0
    }
}
