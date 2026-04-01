package com.umc.hellodoctor.feature.chat.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.umc.hellodoctor.R
import com.umc.hellodoctor.databinding.FragmentFieldModeBinding
import com.umc.hellodoctor.feature.chat.data.ai.SymptomSummaryResponse
import com.umc.hellodoctor.feature.userinfo.UserInfoViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
@Suppress("TooManyFunctions")
class FieldModeFragment : Fragment() {
    private var _binding: FragmentFieldModeBinding? = null
    private val binding get() = _binding!!

    private val chatViewModel: ChatViewModel by activityViewModels()
    private val userInfoViewModel: UserInfoViewModel by activityViewModels()

    // 현재 표시 언어 (true: 한글, false: 원본 언어)
    private var isShowingKorean = false
    private var currentSummaryResponse: SymptomSummaryResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFieldModeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()

        // 유저 프로필 조회 및 표시
        userInfoViewModel.getMyProfile()
        observeUserProfile()

        // 이미 존재하는 데이터가 있으면 먼저 표시
        chatViewModel.symptomSummaryResponse.value?.let { summaryResponse ->
            currentSummaryResponse = summaryResponse
            isShowingKorean = false
            displaySymptomSummary(summaryResponse)
        }
        // 이후 업데이트를 관찰
        observeSymptomSummary()
    }

    private fun setupViews() {
        // 돌아가기 버튼 클릭 리스너
        binding.btnBackToChat.setOnClickListener {
            chatViewModel.resetSession()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // 번역하기 이미지 클릭 리스너
        binding.imageView.setOnClickListener {
            toggleLanguage()
        }
    }

    /**
     * 유저 프로필 관찰 및 UI 업데이트
     */
    private fun observeUserProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            userInfoViewModel.uiState.collect { state ->
                state.profileData?.let { profile ->
                    // 이름
                    binding.tvPatientName.text = profile.displayName

                    // 성별 (MALE/FEMALE -> 남자/여자)
                    binding.tvPatientGender.text =
                        when (profile.gender.uppercase()) {
                            "MALE" -> "남자"
                            "FEMALE" -> "여자"
                            else -> profile.gender
                        }

                    // 생년월일 (yyyy-MM-dd -> yyyy.MM.dd)
                    binding.tvPatientBirth.text = profile.birthDate.replace("-", ".")

                    // 혈액형
                    binding.tvPatientBloodType.text = "${profile.bloodType}형"

                    // 복용약 유무 (tags에서 "복용약" 관련 태그 확인)
                    val hasMedication =
                        profile.tags.any {
                            it.contains("복용약") || it.contains("medication") || it.contains("약물")
                        }
                    binding.tvPatientMedication.text = if (hasMedication) "있음" else "없음"
                }
            }
        }
    }

    /**
     * 증상 요약 LiveData 관찰
     */
    private fun observeSymptomSummary() {
        chatViewModel.symptomSummaryResponse.observe(viewLifecycleOwner) { summaryResponse ->
            if (summaryResponse != null) {
                currentSummaryResponse = summaryResponse
                // 초기값: 원본 언어로 표시
                isShowingKorean = false
                displaySymptomSummary(summaryResponse)
            }
        }
    }

    /**
     * 언어 토글 - 한글/원본 언어 전환
     */
    private fun toggleLanguage() {
        if (currentSummaryResponse == null) return

        isShowingKorean = !isShowingKorean
        displaySymptomSummary(currentSummaryResponse!!)
    }

    /**
     * 증상 요약을 TableLayout에 표시
     */
    private fun displaySymptomSummary(summaryResponse: SymptomSummaryResponse) {
        val tableLayout = binding.tableSymptomItems
        tableLayout.removeAllViews()

        try {
            if (isShowingKorean) {
                displayKoreanSummary(summaryResponse)
            } else {
                displayOriginalSummary(summaryResponse)
            }
        } catch (_: Exception) {
            // 에러 처리
        }
    }

    /**
     * 한글 요약 표시
     */
    private fun displayKoreanSummary(summaryResponse: SymptomSummaryResponse) {
        binding.langTextView.text = "🇰🇷 한국어 (KO)"
        summaryResponse.korean?.forEach { item ->
            addTableRow(binding.tableSymptomItems, item.category, item.description)
        }
    }

    /**
     * 원본 언어 요약 표시
     */
    private fun displayOriginalSummary(summaryResponse: SymptomSummaryResponse) {
        summaryResponse.original?.let { original ->
            binding.langTextView.text =
                "📋 ${original.languageName} (${original.language.uppercase()})"
            original.data?.forEach { item ->
                addTableRow(binding.tableSymptomItems, item.category, item.description)
            }
        }
    }

    /**
     * TableLayout에 행 추가
     */
    private fun addTableRow(
        tableLayout: TableLayout,
        category: String,
        description: String,
    ) {
        val row =
            TableRow(requireContext()).apply {
                layoutParams =
                    TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT,
                    )
            }

        // 카테고리 (좌측)
        val categoryView =
            TextView(requireContext()).apply {
                text = category
                setTextColor(resources.getColor(R.color.color_text_main, null))
                setPadding(PADDING_ZERO, PADDING_SMALL, PADDING_SMALL, PADDING_SMALL)
                layoutParams =
                    TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT,
                    )
            }

        // 설명 (우측, 확장)
        val descriptionView =
            TextView(requireContext()).apply {
                text = description
                setTextColor(resources.getColor(R.color.color_text_main, null))
                setPadding(PADDING_SMALL, PADDING_SMALL, PADDING_ZERO, PADDING_SMALL)
                layoutParams =
                    TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TABLE_WEIGHT_DESCRIPTION,
                    )
            }

        row.addView(categoryView)
        row.addView(descriptionView)
        tableLayout.addView(row)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val PADDING_ZERO = 0
        private const val PADDING_SMALL = 8
        private const val TABLE_WEIGHT_DESCRIPTION = 1f
    }
}
