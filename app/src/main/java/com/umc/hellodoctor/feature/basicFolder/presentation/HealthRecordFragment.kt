package com.umc.hellodoctor.feature.basicFolder.presentation

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.umc.hellodoctor.R
import com.umc.hellodoctor.databinding.FragmentHealthRecordBinding
import java.util.Calendar

class HealthRecordFragment : Fragment(R.layout.fragment_health_record) {

    private var _binding: FragmentHealthRecordBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHealthRecordBinding.bind(view)

        // 공통: 단일 선택
        fun singleSelect(selected: MaterialButton, group: List<MaterialButton>) {
            group.forEach { it.isSelected = (it == selected) }
        }

        // 생년월일 DatePicker
        fun showBirthPicker() {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    binding.etBirth.setText("%04d-%02d-%02d".format(y, m + 1, d))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // 입력칸 클릭 + 캘린더 아이콘 클릭
        binding.etBirth.setOnClickListener { showBirthPicker() }
        binding.tilBirth.setEndIconOnClickListener { showBirthPicker() }

        // 성별(단일)
        val genderBtns = listOf(binding.btnFemale, binding.btnMale)
        genderBtns.forEach { b ->
            b.setOnClickListener { singleSelect(b, genderBtns) }
        }

        // 혈액형(단일) + 직접입력 시 입력칸 노출
        val bloodBtns = listOf(
            binding.btnBloodA,
            binding.btnBloodB,
            binding.btnBloodO,
            binding.btnBloodAB,
            binding.btnBloodOther
        )

        fun updateBloodOtherUi() {
            val isOther = binding.btnBloodOther.isSelected
            binding.tilBloodOther.isVisible = isOther
            if (!isOther) binding.etBloodOther.setText("")
        }

        bloodBtns.forEach { b ->
            b.setOnClickListener {
                singleSelect(b, bloodBtns)
                updateBloodOtherUi()
            }
        }

        // Q1 알레르기 유무(단일) + Yes일 때만 1-1 노출
        val allergyYesNo = listOf(binding.btnAllergyYes, binding.btnAllergyNo)

        fun updateAllergyUi() {
            val yes = binding.btnAllergyYes.isSelected
            binding.groupAllergyDetail.isVisible = yes

            // No로 바꾸면 상세 선택값 리셋
            if (!yes) {
                listOf(
                    binding.btnAllergyAntibiotic,
                    binding.btnAllergyNsaid,
                    binding.btnAllergyVaccine,
                    binding.btnAllergyLocal,
                    binding.btnAllergyOther
                ).forEach { it.isSelected = false }
            }
        }

        allergyYesNo.forEach { b ->
            b.setOnClickListener {
                singleSelect(b, allergyYesNo)
                updateAllergyUi()
            }
        }

        // Q1-1 알레르기 종류(복수 선택: 토글)
        listOf(
            binding.btnAllergyAntibiotic,
            binding.btnAllergyNsaid,
            binding.btnAllergyVaccine,
            binding.btnAllergyLocal,
            binding.btnAllergyOther
        ).forEach { b ->
            b.setOnClickListener { b.isSelected = !b.isSelected }
        }

        // Q2 복용약/영양제(단일)
        val medsYesNo = listOf(binding.btnMedsYes, binding.btnMedsNo)
        medsYesNo.forEach { b ->
            b.setOnClickListener { singleSelect(b, medsYesNo) }
        }

        // Q3 임신/수유(단일)
        val pregYesNo = listOf(binding.btnPregYes, binding.btnPregNo)
        pregYesNo.forEach { b ->
            b.setOnClickListener { singleSelect(b, pregYesNo) }
        }

        // 초기 UI 상태 정리
        updateBloodOtherUi()
        updateAllergyUi()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
