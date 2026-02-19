package com.umc.hellodoctor.feature.basicFolder.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.umc.hellodoctor.R
import com.umc.hellodoctor.core.util.showToast
import com.umc.hellodoctor.databinding.FragmentHomeMenuBinding
import com.umc.hellodoctor.feature.userinfo.UserInfoViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeMenuFragment : Fragment() {

    private var _binding: FragmentHomeMenuBinding? = null
    private val binding get() = _binding!!
    private val userInfoViewModel: UserInfoViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 이미 저장된 프로필이 없으면 조회
        if (userInfoViewModel.uiState.value.profileData == null) {
            userInfoViewModel.getMyProfile()
        }

        binding.menuHospital.setOnClickListener {

            showToast(requireContext(),"의료기관 검색 준비중입니다")
        }

        binding.menuPrescription.setOnClickListener {
            findNavController().navigate(R.id.action_homeMenuFragment_to_drugAlarmFragment)
        }

        binding.menuSymptom.setOnClickListener {
            findNavController().navigate(R.id.action_homeMenuFragment_to_symptomMenuFragment)
        }

        binding.menuMyPage.setOnClickListener {
            showToast(requireContext(),"마이페이지 준비중입니다")
        }

        // 프로필 데이터 상태 관찰
        observeProfileData()
    }

    private fun observeProfileData() {
        viewLifecycleOwner.lifecycleScope.launch {
            userInfoViewModel.uiState.collect { state ->
                state.profileData?.let { profile ->
                    Log.d("HomeMenuFragment", "프로필 로드됨: ${profile.displayName}")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
