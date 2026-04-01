package com.umc.hellodoctor.feature.userinfo

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.umc.hellodoctor.databinding.FragmentUserInfoBinding
import com.umc.hellodoctor.feature.auth.presentation.AuthViewModel
import com.umc.hellodoctor.feature.userinfo.data.model.AllergyType
import com.umc.hellodoctor.feature.userinfo.data.model.BloodType
import com.umc.hellodoctor.feature.userinfo.data.model.GenderType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
@Suppress("TooManyFunctions")
class UserInfoFragment : Fragment() {
    private var _binding: FragmentUserInfoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserInfoViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentUserInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupValidation()
    }

    private fun setupClickListeners() {
        // 이름 입력
        binding.etName.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                    // no-op
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {
                    // no-op
                }

                override fun afterTextChanged(s: Editable?) {
                    viewModel.setName(s.toString())
                }
            },
        )

        // 다음 버튼 클릭
        binding.btnNext.setOnClickListener {
            viewModel.createProfile()
        }

        // === 기본정보 섹션 ===
        setupGenderButtons()
        setupBirthDatePicker()
        setupBloodTypeButtons()

        // === 건강상태 섹션 ===
        setupAllergyButtons()
        setupAllergyDetailButtons()
        setupMedicationButtons()
        setupPregnancyButtons()
    }

    // ========== 기본정보 섹션 ==========

    /** 성별 선택 (단일선택) */
    private fun setupGenderButtons() {
        binding.btnFemale.setOnClickListener {
            viewModel.setGender(GenderType.FEMALE)
        }
        binding.btnMale.setOnClickListener {
            viewModel.setGender(GenderType.MALE)
        }
    }

    /** 생년월일 선택 */
    private fun setupBirthDatePicker() {
        binding.tilBirth.setEndIconOnClickListener { showDatePicker() }
        binding.etBirth.setOnClickListener { showDatePicker() }
    }

    /** 혈액형 선택 (단일선택) */
    private fun setupBloodTypeButtons() {
        binding.btnBloodA.setOnClickListener { viewModel.setBloodType(BloodType.A) }
        binding.btnBloodB.setOnClickListener { viewModel.setBloodType(BloodType.B) }
        binding.btnBloodO.setOnClickListener { viewModel.setBloodType(BloodType.O) }
        binding.btnBloodAB.setOnClickListener { viewModel.setBloodType(BloodType.AB) }

        // 혈액형 직접 입력
        binding.btnBloodOther.setOnClickListener {
            val isShown = binding.tilBloodOther.visibility == View.VISIBLE
            if (isShown) {
                binding.tilBloodOther.visibility = View.GONE
                binding.etBloodOther.setText("")
                viewModel.setBloodType(null)
                viewModel.setBloodTypeOther("")
            } else {
                binding.tilBloodOther.visibility = View.VISIBLE
                viewModel.setBloodType(BloodType.OTHER)
            }
        }

        // 혈액형 직접 입력 텍스트 변경 리스너 (실시간 감지)
        binding.etBloodOther.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                    // no-op
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {
                    // no-op
                }

                override fun afterTextChanged(s: Editable?) {
                    viewModel.setBloodTypeOther(s.toString())
                }
            },
        )
    }

    // ========== 건강상태 섹션 ==========

    /** Q1: 알레르기 여부 선택 (단일선택) */
    private fun setupAllergyButtons() {
        binding.btnAllergyYes.setOnClickListener {
            viewModel.setAllergy(true)
            binding.groupAllergyDetail.visibility = View.VISIBLE
        }
        binding.btnAllergyNo.setOnClickListener {
            viewModel.setAllergy(false)
            binding.groupAllergyDetail.visibility = View.GONE
        }
    }

    /** Q1-1: 알레르기 상세 선택 (다중선택) */
    private fun setupAllergyDetailButtons() {
        binding.btnAllergyAntibiotic.setOnClickListener {
            viewModel.toggleAllergyDetail(AllergyType.ANTIBIOTIC)
        }
        binding.btnAllergyNsaid.setOnClickListener {
            viewModel.toggleAllergyDetail(AllergyType.PAINKILLER)
        }
        binding.btnAllergyVaccine.setOnClickListener {
            viewModel.toggleAllergyDetail(AllergyType.VACCINE)
        }
        binding.btnAllergyLocal.setOnClickListener {
            viewModel.toggleAllergyDetail(AllergyType.ANESTHETIC)
        }
        binding.btnAllergyOther.setOnClickListener {
            viewModel.toggleAllergyDetail(AllergyType.OTHER)
        }
    }

    /** Q2: 처방약/영양제 복용 여부 (단일선택) */
    private fun setupMedicationButtons() {
        binding.btnMedsYes.setOnClickListener {
            viewModel.setMedication(true)
        }
        binding.btnMedsNo.setOnClickListener {
            viewModel.setMedication(false)
        }
    }

    /** Q3: 임신/수유 여부 (단일선택) */
    private fun setupPregnancyButtons() {
        binding.btnPregYes.setOnClickListener {
            viewModel.setPregnancy(true)
        }
        binding.btnPregNo.setOnClickListener {
            viewModel.setPregnancy(false)
        }
    }

    // ========== 검증 및 UI 업데이트 ==========

    private fun setupValidation() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // 혈액형 검증: OTHER 선택 시 직접 입력 필드가 비어있지 않아야 함
                val bloodTypeValid =
                    when (state.bloodType) {
                        BloodType.OTHER -> state.bloodTypeOther.isNotBlank()
                        null -> false
                        else -> true
                    }

                // 기본정보 필수항목 체크
                val basicInfoValid =
                    state.name.isNotBlank() &&
                        state.gender != null &&
                        state.birthDate != null &&
                        bloodTypeValid

                // 건강상태 필수항목 체크
                val healthStateValid =
                    state.allergy != null &&
                        state.medication != null &&
                        state.pregnancy != null

                // Q1-1: 알레르기 'Yes'인 경우 상세항목 최소 1개 선택 필수
                val allergyDetailValid =
                    if (state.allergy == true) {
                        state.allergyDetails.isNotEmpty()
                    } else {
                        true
                    }

                val isFormValid = basicInfoValid && healthStateValid && allergyDetailValid

                // 다음 버튼 활성화 상태 업데이트 (로딩 중이 아니고 폼이 유효할 때)
                binding.btnNext.isEnabled = isFormValid && !state.isLoading

                // UI 상태 업데이트
                updateAllSectionUI(state)

                // API 응답 처리
                handleApiResponse(state)
            }
        }
    }

    /** 모든 섹션의 UI 상태 업데이트 */
    private fun updateAllSectionUI(state: UserInfoUiState) {
        updateGenderUI(state)
        updateBloodTypeUI(state)
        updateAllergyUI(state)
        updateAllergyDetailUI(state)
        updateMedicationUI(state)
        updatePregnancyUI(state)
    }

    // ========== API 응답 관찰 ==========

    private fun handleApiResponse(state: UserInfoUiState) {
        // 프로필 생성 성공 처리
        if (state.isSuccess) {
            Toast.makeText(requireContext(), "프로필이 성공적으로 생성되었습니다!", Toast.LENGTH_SHORT).show()

            // AuthViewModel에 프로필 생성 완료 알림
            authViewModel.onProfileCreated()
        }

        // 에러 처리
        state.errorMessage?.let { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    // ========== UI 업데이트 함수들 ==========

    /** 성별 UI 업데이트 (단일선택) */
    private fun updateGenderUI(state: UserInfoUiState) {
        binding.btnFemale.isSelected = state.gender == GenderType.FEMALE
        binding.btnMale.isSelected = state.gender == GenderType.MALE
    }

    /** 혈액형 UI 업데이트 (단일선택) */
    private fun updateBloodTypeUI(state: UserInfoUiState) {
        binding.btnBloodA.isSelected = state.bloodType == BloodType.A
        binding.btnBloodB.isSelected = state.bloodType == BloodType.B
        binding.btnBloodO.isSelected = state.bloodType == BloodType.O
        binding.btnBloodAB.isSelected = state.bloodType == BloodType.AB
        binding.btnBloodOther.isSelected = state.bloodType == BloodType.OTHER

        // 혈액형 직접 입력 필드 가시성
        binding.tilBloodOther.visibility = if (state.bloodType == BloodType.OTHER) View.VISIBLE else View.GONE
    }

    /** 알레르기 여부 UI 업데이트 (단일선택) */
    private fun updateAllergyUI(state: UserInfoUiState) {
        binding.btnAllergyYes.isSelected = state.allergy == true
        binding.btnAllergyNo.isSelected = state.allergy == false
    }

    /** 알레르기 상세 UI 업데이트 (다중선택) */
    private fun updateAllergyDetailUI(state: UserInfoUiState) {
        binding.btnAllergyAntibiotic.isSelected = state.allergyDetails.contains(AllergyType.ANTIBIOTIC)
        binding.btnAllergyNsaid.isSelected = state.allergyDetails.contains(AllergyType.PAINKILLER)
        binding.btnAllergyVaccine.isSelected = state.allergyDetails.contains(AllergyType.VACCINE)
        binding.btnAllergyLocal.isSelected = state.allergyDetails.contains(AllergyType.ANESTHETIC)
        binding.btnAllergyOther.isSelected = state.allergyDetails.contains(AllergyType.OTHER)
    }

    /** 처방약 여부 UI 업데이트 (단일선택) */
    private fun updateMedicationUI(state: UserInfoUiState) {
        binding.btnMedsYes.isSelected = state.medication == true
        binding.btnMedsNo.isSelected = state.medication == false
    }

    /** 임신/수유 여부 UI 업데이트 (단일선택) */
    private fun updatePregnancyUI(state: UserInfoUiState) {
        binding.btnPregYes.isSelected = state.pregnancy == true
        binding.btnPregNo.isSelected = state.pregnancy == false
    }

    // ========== 날짜 선택 ==========

    private fun showDatePicker() {
        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("생년월일 선택")
                .build()

        datePicker.addOnPositiveButtonClickListener { timestamp ->
            val date = Date(timestamp)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateString = sdf.format(date)
            binding.etBirth.setText(dateString)
            viewModel.setBirthDate(dateString)
        }

        datePicker.show(childFragmentManager, "datePicker")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
