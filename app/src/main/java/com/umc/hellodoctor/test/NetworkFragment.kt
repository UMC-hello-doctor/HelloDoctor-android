// NetworkFragment.kt
package com.umc.hellodoctor.core.network.status

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.umc.hellodoctor.databinding.FragmentNetworkBinding
import kotlinx.coroutines.launch

class NetworkFragment : Fragment() {

    private var _binding: FragmentNetworkBinding? = null
    private val binding get() = _binding!!

    // 여러 화면에서 공유하고 싶으면 activityViewModels, 이 프래그먼트 전용이면 viewModels 사용
    private val networkViewModel: NetworkViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNetworkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 동그라미 모양 인디케이터 초기 세팅
        val indicatorBackground = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
        }
        binding.viewIndicator.background = indicatorBackground

        // StateFlow 수집해서 UI 갱신
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(
                androidx.lifecycle.Lifecycle.State.STARTED
            ) {
                launch {
                    networkViewModel.status.collect { status ->
                        when (status) {
                            NetworkStatus.Available -> {
                                binding.tvNetworkStatus.text = "온라인 상태입니다."
                                indicatorBackground.setColor(
                                    ContextCompat.getColor(requireContext(), android.R.color.holo_green_light)
                                )
                            }
                            NetworkStatus.Lost -> {
                                binding.tvNetworkStatus.text = "네트워크 연결이 끊어졌습니다."
                                indicatorBackground.setColor(
                                    ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)
                                )
                            }
                        }
                        binding.viewIndicator.invalidate()
                    }
                }

                // Boolean 플래그도 필요하면 같이 사용 가능
                launch {
                    networkViewModel.isConnected.collect { isConnected ->
                        // 예: 연결 안 됐을 때 view alpha 낮추기 등
                        binding.tvNetworkStatus.alpha = if (isConnected) 1f else 0.7f
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
