package com.umc.hellodoctor.feature.menu.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.umc.hellodoctor.R
import com.umc.hellodoctor.core.util.showToast
import com.umc.hellodoctor.databinding.FragmentMyPageMenuBinding

class MyPageMenuFragment : Fragment() {

    private var _binding: FragmentMyPageMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBasicInfo.searchText.text = getString(R.string.language_change)
        binding.btnHealthRecord.searchText.text = getString(R.string.health_status_record)

        binding.btnBasicInfo.searchIcon.setImageResource(R.drawable.ic_g_translate)
        binding.btnHealthRecord.searchIcon.setImageResource(R.drawable.ic_mypage)

        binding.backIcon.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnBasicInfo.root.setOnClickListener {
            findNavController().navigate(R.id.action_myPageMenuFragment_to_languageSelectFragment)
        }

        binding.btnHealthRecord.root.setOnClickListener {
            showToast(requireContext(), getString(R.string.mypage_preparing))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
