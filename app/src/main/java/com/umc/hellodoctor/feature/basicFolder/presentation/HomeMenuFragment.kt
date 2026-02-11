package com.umc.hellodoctor.feature.basicFolder.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.umc.hellodoctor.R
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
            Toast.makeText(requireContext(), "의료기관 검색", Toast.LENGTH_SHORT).show()
        }

        binding.menuPrescription.setOnClickListener {
            Toast.makeText(requireContext(), "처방약 관리", Toast.LENGTH_SHORT).show()
        }

        binding.menuSymptom.setOnClickListener {
            Toast.makeText(requireContext(), "증상 번역", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_homeMenuFragment_to_symptomMenuFragment)
        }

        binding.menuMyPage.setOnClickListener {
            Toast.makeText(requireContext(), "마이페이지", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
