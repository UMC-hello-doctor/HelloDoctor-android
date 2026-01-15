// NotificationFragment.kt
package com.umc.hellodoctor.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.umc.hellodoctor.core.notification.NotificationHelper
import com.umc.hellodoctor.databinding.FragmentNotificationBinding

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 기본 알림 (NotificationHelper 기본 문구 사용)
        binding.btnShowDefaultNotification.setOnClickListener {
            NotificationHelper.showDefaultAlarm(requireContext())
        }

        // 커스텀 타이틀/메시지 알림
        binding.btnShowCustomNotification.setOnClickListener {
            NotificationHelper.showNotification(
                context = requireContext(),
                notificationId = 2001,
                title = "진료 예약 알림",
                message = "10분 뒤에 예약된 진료가 있습니다."
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
