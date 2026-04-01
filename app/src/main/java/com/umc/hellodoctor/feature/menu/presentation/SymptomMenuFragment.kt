package com.umc.hellodoctor.feature.menu.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.umc.hellodoctor.R
import com.umc.hellodoctor.databinding.FragmentSymptomMenuBinding

class SymptomMenuFragment : Fragment() {
    private var _binding: FragmentSymptomMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSymptomMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNewChat.searchText.text = "새 채팅"
        binding.btnHistory.searchText.text = "기록"

        binding.btnNewChat.searchIcon.setImageResource(R.drawable.outline_add_comment_24)
        binding.btnHistory.searchIcon.setImageResource(R.drawable.outline_article_24)

        binding.backIcon.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnNewChat.root.setOnClickListener {
            findNavController().navigate(R.id.action_symptomMenuFragment_to_chatFragment)
        }

        binding.btnHistory.root.setOnClickListener {
            findNavController().navigate(R.id.action_symptomMenuFragment_to_historyFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
