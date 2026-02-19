package com.umc.hellodoctor.feature.language.presentation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.umc.hellodoctor.R
import com.umc.hellodoctor.databinding.FragmentLanguageSelectBinding

class LanguageSelectFragment : Fragment(R.layout.fragment_language_select) {

    private var _binding: FragmentLanguageSelectBinding? = null
    private val binding get() = _binding!!
    private lateinit var languageManager: LanguageManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLanguageSelectBinding.bind(view)
        languageManager = LanguageManager(requireContext())

        val items = listOf(
            LanguageItem(getString(R.string.language_korean), R.drawable.flag_korea),
            LanguageItem(getString(R.string.language_english), R.drawable.flag_america),
            LanguageItem(getString(R.string.language_japanese), R.drawable.flag_japan),
            LanguageItem(getString(R.string.language_chinese), R.drawable.flag_china),
            LanguageItem(getString(R.string.language_vietnamese), R.drawable.flag_vietnam)
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

        // 저장하기 버튼 클릭 처리
        binding.btnSaveLanguage.setOnClickListener {
            val selectedLanguage = binding.selectLanguage.text.toString().trim()

            if (selectedLanguage.isEmpty()) {
            } else {
                // 선택된 언어 저장
                languageManager.saveLanguage(selectedLanguage)

                // 앱 재시작하여 언어 변경 적용
                val intent = requireActivity().intent
                requireActivity().finish()
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

