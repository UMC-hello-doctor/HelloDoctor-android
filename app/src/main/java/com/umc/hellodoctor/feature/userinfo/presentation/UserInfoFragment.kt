package com.umc.hellodoctor.feature.userinfo.presentation


import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.umc.hellodoctor.R
import com.umc.hellodoctor.databinding.FragmentUserInfoBinding
import com.umc.hellodoctor.util.singleSelect
import com.umc.hellodoctor.util.toggleSelect
import com.umc.hellodoctor.util.showBirthDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.umc.hellodoctor.feature.userinfo.presentation.AllergyTag

@AndroidEntryPoint
class UserInfoFragment : Fragment(R.layout.fragment_user_info) {

    private var _binding: FragmentUserInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserInfoViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserInfoBinding.bind(view)

        initListeners()
        collectState()
    }

    private fun initListeners() = with(binding) {
        // 이름
        etName.addTextChangedListener(SimpleTextWatcher { text ->
            viewModel.onNameChanged(text)
        })

        // 성별
        val genderGroup = listOf(btnFemale, btnMale)
        btnFemale.setOnClickListener {
            singleSelect(btnFemale, genderGroup)
            viewModel.onGenderSelected(Gender.FEMALE)
        }
        btnMale.setOnClickListener {
            singleSelect(btnMale, genderGroup)
            viewModel.onGenderSelected(Gender.MALE)
        }

        // 생년월일 (TextInputLayout endIcon / EditText 클릭 둘 다 처리)
        val openBirthPicker = {
            showBirthDatePicker(requireContext(), viewModel.uiState.value.birthDate) { picked ->
                viewModel.onBirthDateSelected(picked)
            }
        }
        tilBirth.setEndIconOnClickListener { openBirthPicker() }
        etBirth.setOnClickListener { openBirthPicker() }

        // 혈액형
        val bloodGroup = listOf(btnBloodA, btnBloodB, btnBloodO, btnBloodAB, btnBloodOther)
        btnBloodA.setOnClickListener {
            singleSelect(btnBloodA, bloodGroup)
            viewModel.onBloodTypeSelected(BloodType.A)
        }
        btnBloodB.setOnClickListener {
            singleSelect(btnBloodB, bloodGroup)
            viewModel.onBloodTypeSelected(BloodType.B)
        }
        btnBloodO.setOnClickListener {
            singleSelect(btnBloodO, bloodGroup)
            viewModel.onBloodTypeSelected(BloodType.O)
        }
        btnBloodAB.setOnClickListener {
            singleSelect(btnBloodAB, bloodGroup)
            viewModel.onBloodTypeSelected(BloodType.AB)
        }
        btnBloodOther.setOnClickListener {
            singleSelect(btnBloodOther, bloodGroup)
            viewModel.onBloodTypeSelected(BloodType.OTHER)
        }

        etBloodOther.addTextChangedListener(SimpleTextWatcher { text ->
            viewModel.onBloodOtherTextChanged(text)
        })

        // 알레르기 유무
        val allergyYesNo = listOf(btnAllergyYes, btnAllergyNo)
        btnAllergyYes.setOnClickListener {
            singleSelect(btnAllergyYes, allergyYesNo)
            viewModel.onAllergySelected(YesNo.YES)
        }
        btnAllergyNo.setOnClickListener {
            singleSelect(btnAllergyNo, allergyYesNo)
            viewModel.onAllergySelected(YesNo.NO)
        }

        val allergyTagButtons: Map<AllergyTag, MaterialButton> = mapOf(
            AllergyTag.ANTIBIOTIC to binding.btnAllergyAntibiotic,
            AllergyTag.NSAID to binding.btnAllergyNsaid,
            AllergyTag.VACCINE to binding.btnAllergyVaccine,
            AllergyTag.LOCAL_ANESTHETIC to binding.btnAllergyLocal,
            AllergyTag.OTHER to binding.btnAllergyOther
        )

        allergyTagButtons.forEach { (tag, btn) ->
            btn.setOnClickListener {
                toggleSelect(button = btn)
                viewModel.toggleAllergyTag(tag)
            }
        }

        // 복용약/영양제
        val medsYesNo = listOf(btnMedsYes, btnMedsNo)
        btnMedsYes.setOnClickListener {
            singleSelect(btnMedsYes, medsYesNo)
            viewModel.onMedsSelected(YesNo.YES)
        }
        btnMedsNo.setOnClickListener {
            singleSelect(btnMedsNo, medsYesNo)
            viewModel.onMedsSelected(YesNo.NO)
        }

        // 임신/수유
        val pregYesNo = listOf(btnPregYes, btnPregNo)
        btnPregYes.setOnClickListener {
            singleSelect(btnPregYes, pregYesNo)
            viewModel.onPregSelected(YesNo.YES)
        }
        btnPregNo.setOnClickListener {
            singleSelect(btnPregNo, pregYesNo)
            viewModel.onPregSelected(YesNo.NO)
        }

        // 유저 정보 출력
//        btnNext.setOnClickListener {
//            val state = viewModel.uiState.value
//            android.util.Log.d("UserInfo", state.toString())
//        }
        btnNext.setOnClickListener {
            val token = "여기에_액세스토큰" // 일단 테스트는 하드코딩
            viewModel.submitProfile(token)
        }
    }

    private fun collectState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.submitState.collect { s ->
                        android.util.Log.d("UserInfoAPI", "submitState=$s")
                    }
                }

                // 1) uiState 관찰
                launch {
                    viewModel.uiState.collect { state ->
                        // 이름
                        if (binding.etName.text?.toString() != state.name) {
                            binding.etName.setText(state.name)
                            binding.etName.setSelection(state.name.length)
                        }

                        // 생년월일
                        if (binding.etBirth.text?.toString() != state.birthDate) {
                            binding.etBirth.setText(state.birthDate)
                        }

                        // 혈액형 직접입력 노출
                        binding.tilBloodOther.isVisible = state.isBloodOtherVisible
                        if (!state.isBloodOtherVisible) {
                            if (binding.etBloodOther.text?.toString().orEmpty().isNotBlank()) {
                                binding.etBloodOther.setText("")
                            }
                        } else {
                            if (binding.etBloodOther.text?.toString() != state.bloodTypeOtherText) {
                                binding.etBloodOther.setText(state.bloodTypeOtherText)
                                binding.etBloodOther.setSelection(state.bloodTypeOtherText.length)
                            }
                        }

                        // 알레르기 상세 노출
                        binding.groupAllergyDetail.isVisible = state.isAllergyDetailVisible

                        // 알레르기 태그 버튼 매핑 (AllergyTag -> Button)
                        val allergyTagButtons: Map<AllergyTag, MaterialButton> = mapOf(
                            AllergyTag.ANTIBIOTIC to binding.btnAllergyAntibiotic,
                            AllergyTag.NSAID to binding.btnAllergyNsaid,
                            AllergyTag.VACCINE to binding.btnAllergyVaccine,
                            AllergyTag.LOCAL_ANESTHETIC to binding.btnAllergyLocal,
                            AllergyTag.OTHER to binding.btnAllergyOther
                        )

                        allergyTagButtons.forEach { (tag, btn) ->
                            btn.isSelected = state.allergySelectedTags.contains(tag)
                        }

                        // 성별 버튼 표시
                        binding.btnFemale.isSelected = (state.gender == Gender.FEMALE)
                        binding.btnMale.isSelected = (state.gender == Gender.MALE)

                        // 혈액형 버튼 표시
                        binding.btnBloodA.isSelected = (state.bloodType == BloodType.A)
                        binding.btnBloodB.isSelected = (state.bloodType == BloodType.B)
                        binding.btnBloodO.isSelected = (state.bloodType == BloodType.O)
                        binding.btnBloodAB.isSelected = (state.bloodType == BloodType.AB)
                        binding.btnBloodOther.isSelected = (state.bloodType == BloodType.OTHER)

                        // 알레르기 yes/no
                        binding.btnAllergyYes.isSelected = (state.hasAllergy == YesNo.YES)
                        binding.btnAllergyNo.isSelected = (state.hasAllergy == YesNo.NO)

                        // 복용약 yes/no
                        binding.btnMedsYes.isSelected = (state.hasMeds == YesNo.YES)
                        binding.btnMedsNo.isSelected = (state.hasMeds == YesNo.NO)

                        // 임신/수유 yes/no
                        binding.btnPregYes.isSelected = (state.isPregnantOrBreastfeeding == YesNo.YES)
                        binding.btnPregNo.isSelected = (state.isPregnantOrBreastfeeding == YesNo.NO)
                    }
                }

                // 2) isSaveEnabled 관찰
                launch {
                    viewModel.isSaveEnabled.collect { enabled ->
                        binding.btnNext.isEnabled = enabled
                        binding.btnNext.alpha = if (enabled) 1f else 0.4f
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// TextWatcher 간단 래퍼 (복붙용)
private class SimpleTextWatcher(
    private val onChanged: (String) -> Unit
) : android.text.TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
    override fun afterTextChanged(s: android.text.Editable?) {
        onChanged(s?.toString().orEmpty())
    }
}

