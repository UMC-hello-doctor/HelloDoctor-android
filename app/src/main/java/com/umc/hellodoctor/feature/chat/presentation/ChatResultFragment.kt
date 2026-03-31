package com.umc.hellodoctor.feature.chat.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.umc.hellodoctor.R
import com.umc.hellodoctor.databinding.FragmentChatResultContainerBinding
import com.umc.hellodoctor.feature.navermap.presentation.NaverMapFragment

/**
 * 채팅 결과 화면 Fragment
 * 문진표와 병원 지도를 버튼으로 전환
 */
class ChatResultFragment : Fragment() {
    private var _binding: FragmentChatResultContainerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by activityViewModels()
    private val args: ChatResultFragmentArgs by navArgs()

    private var currentFragmentType: FragmentType = FragmentType.FIELD_MODE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChatResultContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()

        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        val sessionId = args.sessionId
        Log.d("ChatResultFragment", "========== ChatResultFragment 세션 처리 ==========")
        Log.d("ChatResultFragment", "전달받은 sessionId: $sessionId")
        Log.d("ChatResultFragment", "sessionId null 여부: ${sessionId == null}")

        if (sessionId != null) {
            // 저장된 세션 로드
            Log.d("ChatResultFragment", "저장된 세션 로드 시작: $sessionId")
            viewModel.loadSessionById(sessionId)
            Log.d("ChatResultFragment", "저장된 세션 로드 완료")
        } else {
            // 새로운 채팅 세션 (기존 로직)
            Log.d("ChatResultFragment", "새로운 세션 초기화 시작")
            viewModel.resetSession()
            Log.d("ChatResultFragment", "새로운 세션 초기화 완료")
        }

        // 초기 화면은 문진표
        currentFragmentType = FragmentType.FIELD_MODE
        showFragment(FragmentType.FIELD_MODE)
        // 초기 버튼 스타일 설정
        updateButtonStyles(FragmentType.FIELD_MODE)
        Log.d("ChatResultFragment", "========== ChatResultFragment 초기화 완료 ==========")
    }

    /**
     * 버튼 클릭 리스너 설정
     */
    private fun setupButtons() {
        binding.btnFieldMode.setOnClickListener {
            showFragment(FragmentType.FIELD_MODE)
            updateButtonStyles(FragmentType.FIELD_MODE)
        }

        binding.btnHospitalMap.setOnClickListener {
            showFragment(FragmentType.HOSPITAL_MAP)
            updateButtonStyles(FragmentType.HOSPITAL_MAP)
        }
    }

    /**
     * Fragment 전환 및 버튼 스타일 변경
     */
    private fun showFragment(type: FragmentType) {
        currentFragmentType = type

        val fragment =
            when (type) {
                FragmentType.FIELD_MODE -> FieldModeFragment()
                FragmentType.HOSPITAL_MAP -> NaverMapFragment()
            }

        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commitAllowingStateLoss()
    }

    /**
     * 선택된 버튼의 스타일 업데이트
     */
    private fun updateButtonStyles(selectedType: FragmentType) {
        when (selectedType) {
            FragmentType.FIELD_MODE -> {
                binding.btnFieldMode.isSelected = true
                binding.btnHospitalMap.isSelected = false
            }
            FragmentType.HOSPITAL_MAP -> {
                binding.btnFieldMode.isSelected = false
                binding.btnHospitalMap.isSelected = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Fragment 타입 enum
     */
    private enum class FragmentType {
        FIELD_MODE,
        HOSPITAL_MAP,
    }
}
