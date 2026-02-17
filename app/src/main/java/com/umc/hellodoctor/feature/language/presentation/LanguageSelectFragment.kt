package com.umc.hellodoctor.feature.language.presentation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.umc.hellodoctor.R
import com.umc.hellodoctor.databinding.FragmentLanguageSelectBinding

class LanguageSelectFragment : Fragment(R.layout.fragment_language_select) {

    private var _binding: FragmentLanguageSelectBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLanguageSelectBinding.bind(view)

        val items = listOf(
            LanguageItem("한국어", R.drawable.flag_korea),
            LanguageItem("영어 (English)", R.drawable.flag_america),
            LanguageItem("일본어 (日本語)", R.drawable.flag_japan),
            LanguageItem("중국어 (中文)", R.drawable.flag_china),
            LanguageItem("베트남어 (tiếng Việt)", R.drawable.flag_vietnam)
        )

        val adapter = LanguageAdapter(requireContext(), items)
        binding.selectLanguage.setAdapter(adapter)

        // 클릭 시 항상 드롭다운 열기
        binding.selectLanguage.setOnClickListener { binding.selectLanguage.showDropDown() }

        // 선택 처리
        binding.selectLanguage.setOnItemClickListener { _, _, position, _ ->
            val selected = adapter.getItem(position) ?: return@setOnItemClickListener
            binding.selectLanguage.setText(selected.label, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

