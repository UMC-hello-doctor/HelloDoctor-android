package com.umc.hellodoctor.feature.drug.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.hellodoctor.R
import com.umc.hellodoctor.databinding.FragmentDrugAlarmBinding
import com.umc.hellodoctor.feature.drug.presentation.adapter.DrugAlarmAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DrugAlarmFragment : Fragment() {

    private val viewModel: DrugPlanViewModel by viewModels()
    private lateinit var binding: FragmentDrugAlarmBinding
    private lateinit var adapter: DrugAlarmAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDrugAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupButton()
        setupHeader()
        observeData()

        viewModel.loadAllPlans()
    }

    private fun setupRecyclerView() {
        adapter = DrugAlarmAdapter { plan ->
            navigateToDrugDetail(plan.id)
        }
        binding.rvDrugAlarms.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@DrugAlarmFragment.adapter
        }
    }

    private fun setupButton() {
        binding.btnAddDrug.setOnClickListener {
            findNavController().navigate(R.id.action_drugAlarmFragment_to_drugSerchFragment)
        }
    }

    private fun setupHeader() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeData() {
        viewModel.plans.observe(viewLifecycleOwner) { plans ->
            adapter.submitList(plans)
        }
    }

    private fun navigateToDrugDetail(planId: String) {
        val action = DrugAlarmFragmentDirections.actionDrugAlarmFragmentToDrugAlarmDetailFragment(planId)
        findNavController().navigate(action)
    }
}
