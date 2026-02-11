package com.umc.hellodoctor.feature.chat.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.hellodoctor.R
import com.umc.hellodoctor.databinding.FragmentChatBinding
import com.umc.hellodoctor.feature.chat.domain.model.Message
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by activityViewModels()
    private lateinit var adapter: MessageAdapter
    private val messages = mutableListOf<Message>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 세션 초기화 (새로운 채팅 시작)
        viewModel.resetSession()

        setupRecyclerView()
        setupObservers()
        setupListeners()

        // 첫 번째 봇 질문 생성 (세션에 저장됨)
        viewModel.addBotQuestion(getString(R.string.first_question))
    }

    /**
     * RecyclerView 설정
     */
    private fun setupRecyclerView() {
        adapter = MessageAdapter(messages)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
    }

    /**
     * ViewModel 옵저버 설정
     */
    private fun setupObservers() {
        // 메시지 리스트 옵저버
        viewModel.messages.observe(viewLifecycleOwner) { newMessages ->
            messages.clear()
            messages.addAll(newMessages)
            adapter.notifyDataSetChanged()
            scrollToBottom()
        }

        // 로딩 상태 옵저버
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnSend.isEnabled = !isLoading
            binding.editInput.isEnabled = !isLoading

            // 로딩 중이면 아이콘 숨기고 로딩 인디케이터 표시
            if (isLoading) {
                binding.btnSend.visibility = View.GONE
                binding.progressLoading.visibility = View.VISIBLE
            } else {
                binding.btnSend.visibility = View.VISIBLE
                binding.progressLoading.visibility = View.GONE
            }
        }

        // 에러 메시지 옵저버
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                viewModel.addBotMessage("오류: $error")
            }
        }

        // 현재 진료과 옵저버
        viewModel.currentDepartment.observe(viewLifecycleOwner) { dept ->
            if (dept.isNotEmpty()) {
                viewModel.addBotMessage("🏥 ${dept} 진료를 시작하겠습니다.")
            }
        }

        // PQRST 완료 옵저버
        viewModel.pqrstComplete.observe(viewLifecycleOwner) { isComplete ->
            if (isComplete) {
                viewModel.addBotMessage("PQRST 분석 완료. 증상을 정리하겠습니다.")
                viewModel.generateAndAddSymptomSummary()

                // 결과 화면으로 이동
                navigateToChatResult()
            }
        }
    }

    /**
     * ChatResultFragment로 이동
     */
    private fun navigateToChatResult() {
        findNavController().navigate(R.id.action_chatFragment_to_chatResultFragment)
    }

    /**
     * 버튼 리스너 설정
     */
    private fun setupListeners() {
        binding.btnSend.setOnClickListener {
            val input = binding.editInput.text.toString().trim()
            if (input.isNotEmpty()) {

                // 진료과 추천 전인지 후인지 확인
                if (viewModel.currentDepartment.value.isNullOrEmpty()) {
                    // 첫 번째 증상 입력 -> 진료과 추천 및 PQRST 시작
                    generateBotResponse(input)
                } else {
                    // 이미 진료과가 선택됨 -> PQRST 질문에 대한 답변 처리
                    handlePQRSTAnswer(input)
                }
                binding.editInput.text?.clear()
            }
        }
    }

    /**
     * 초기 증상 입력에 대한 봇 응답 및 진료과 추천
     */
    private fun generateBotResponse(userInput: String) {
        // 사용자 질문 저장
        viewModel.addUserAnswer(userInput)

        // 사용자 입력에 대한 봇 확인 메시지 (답변이 아닌 일반 메시지)
        val response = "알겠습니다. $userInput 증상이군요. 관련 진료과를 추천하겠습니다."
        viewModel.addBotMessage(response)  // ✅ addBotMessage로 변경 (session.answers에 저장 안 함)

        // 진료과 추천 시작 (AI API 호출 및 PQRST 질문 플로우 시작)
        viewModel.recommendAndStartDepartmentFlow()
    }

    /**
     * PQRST 질문에 대한 사용자 답변 처리
     */
    private fun handlePQRSTAnswer(userAnswer: String) {
        // 사용자 답변 저장
        viewModel.addUserAnswer(userAnswer)

        // 다음 PQRST 단계로 진행
        viewModel.proceedToNextQuestionStage()
    }


    /**
     * 스크롤을 하단으로 이동
     */
    private fun scrollToBottom() {
        _binding?.recyclerView?.post {
            _binding?.recyclerView?.scrollToPosition(messages.size - 1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
