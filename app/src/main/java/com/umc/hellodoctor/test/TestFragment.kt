// TestFragment.kt
package com.umc.hellodoctor.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.umc.hellodoctor.R
import com.umc.hellodoctor.databinding.FragmentTestBinding

class TestFragment : Fragment() {

    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGoNetwork.setOnClickListener {
            findNavController().navigate(
                R.id.action_testFragment_to_networkFragment
            )
        }

        binding.btnGoNaverMap.setOnClickListener {
            findNavController().navigate(
                R.id.action_testFragment_to_naverMapFragment
            )
        }

        binding.btnGoCamera.setOnClickListener {
            findNavController().navigate(
                R.id.action_testFragment_to_cameraFragment
            )
        }

        binding.btnGoNotification.setOnClickListener {
            findNavController().navigate(
                R.id.action_testFragment_to_notificationFragment
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
