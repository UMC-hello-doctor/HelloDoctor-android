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
import androidx.fragment.app.viewModels
import com.umc.hellodoctor.databinding.FragmentFieldModeBinding
import dagger.hilt.android.AndroidEntryPoint
import com.umc.hellodoctor.feature.chat.data.ai.SymptomSummaryResponse
import com.umc.hellodoctor.R

@AndroidEntryPoint
class FieldModeFragment : Fragment() {
    private var _binding: FragmentFieldModeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by activityViewModels()

    // 현재 표시 언어 (true: 한글, false: 원본 언어)
    private var isShowingKorean = false
    private var currentSummaryResponse: SymptomSummaryResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFieldModeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        // 이미 존재하는 데이터가 있으면 먼저 표시
        viewModel.symptomSummaryResponse.value?.let { summaryResponse ->
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
            viewModel.resetSession()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // 번역하기 이미지 클릭 리스너
        binding.imageView.setOnClickListener {
            toggleLanguage()
        }
    }

    /**
     * 증상 요약 LiveData 관찰
     */
    private fun observeSymptomSummary() {
        viewModel.symptomSummaryResponse.observe(viewLifecycleOwner) { summaryResponse ->
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
        try {
            val tableLayout = binding.tableSymptomItems
            tableLayout.removeAllViews()

            if (isShowingKorean) {
                // 한글 버전 표시
                binding.langTextView.text = "🇰🇷 한국어 (KO)"
                summaryResponse.korean?.let { koreanList ->
                    koreanList.forEach { item ->
                        addTableRow(tableLayout, item.category, item.description)
                    }
                }
            } else {
                // 원본 언어 버전 표시
                summaryResponse.original?.let { original ->
                    binding.langTextView.text =
                        "📋 ${original.languageName} (${original.language.uppercase()})"
                    original.data?.forEach { item ->
                        addTableRow(tableLayout, item.category, item.description)
                    }
                }
            }

        } catch (_: Exception) {
            // 에러 처리
        }
    }

    /**
     * TableLayout에 행 추가
     */
    private fun addTableRow(tableLayout: TableLayout, category: String, description: String) {
        val row = TableRow(requireContext()).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // 카테고리 (좌측)
        val categoryView = TextView(requireContext()).apply {
            text = category
            setTextColor(resources.getColor(R.color.color_text_main, null))
            setPadding(0, 8, 8, 8)
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
        }

        // 설명 (우측, 확장)
        val descriptionView = TextView(requireContext()).apply {
            text = description
            setTextColor(resources.getColor(R.color.color_text_main, null))
            setPadding(8, 8, 0, 8)
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
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
}
