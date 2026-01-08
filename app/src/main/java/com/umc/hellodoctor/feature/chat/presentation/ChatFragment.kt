package com.umc.hellodoctor.feature.chat.presentation


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.umc.hellodoctor.databinding.FragmentChatBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        //초기화할것
        binding.chatAi1.text.text = "안녕하세요, 헬로닥터입니다.\n" +
                " 어디가 불편하신가요? \n" +
                "증상을 말씀해주시면 가장 적합한 병원을 찾아드릴게요! \uD83D\uDE4C\uD83C\uDFFB"
        binding.chatAi2.text.text = "통증이 1-10점 중 몇 점인가요?\n" +
                "최근 14일 이내에 해외여행이나 유행성 질환 접촉이 있었나요?"
        binding.chatHuman1.text.text = ""
        binding.chatHuman2.text.text = ""
        binding.chatAi2.root.visibility = View.GONE
        binding.chatHuman1.root.visibility = View.GONE
        binding.chatHuman2.root.visibility = View.GONE


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}