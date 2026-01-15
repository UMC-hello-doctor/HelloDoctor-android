package com.umc.hellodoctor.feature.basicFolder.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.umc.hellodoctor.core.util.toast
import com.umc.hellodoctor.databinding.FragmentHomeMenuBinding

class HomeMenuFragment : Fragment() {

    private var _binding: FragmentHomeMenuBinding? = null
    private val binding get() = _binding!!

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

        binding.menuHospital.setOnClickListener {
            toast("의료기관 검색")
        }

        binding.menuPrescription.setOnClickListener {
            toast("처방약 관리")
        }

        binding.menuSymptom.setOnClickListener {
            toast("증상 번역")
        }

        binding.menuMyPage.setOnClickListener {
            toast("마이페이지")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
