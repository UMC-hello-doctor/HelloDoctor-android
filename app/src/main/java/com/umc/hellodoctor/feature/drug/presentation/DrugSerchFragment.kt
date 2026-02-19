package com.umc.hellodoctor.feature.drug.presentation

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.umc.hellodoctor.R
import com.umc.hellodoctor.databinding.FragmentAddDrugBinding
import com.umc.hellodoctor.feature.drug.data.model.MedicineSearchItem
import com.umc.hellodoctor.feature.drug.presentation.adapter.DrugSearchAdapter
import com.umc.hellodoctor.feature.drug.presentation.adapter.SelectedMedicineAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DrugSerchFragment : Fragment() {

    private var _binding: FragmentAddDrugBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DrugViewModel by viewModels()
    private lateinit var adapter: DrugSearchAdapter
    private val selectedMedicines = mutableListOf<MedicineSearchItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddDrugBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchInput()
        setupBackButton()
        setupConfirmButton()
        setupSelectedListButton()
        observeSearchResults()
    }

    private fun setupRecyclerView() {
        adapter = DrugSearchAdapter { medicine: MedicineSearchItem ->
            onMedicineSelected(medicine)
        }
        binding.rvDrugResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@DrugSerchFragment.adapter
        }
    }

    private fun setupSearchInput() {
        binding.etDrugSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString().orEmpty()
                viewModel.updateInputText(query)
                viewModel.searchMedicines(query)
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        viewModel.inputText.observe(viewLifecycleOwner) { text ->
            val current = binding.etDrugSearch.text?.toString().orEmpty()
            if (current != text) {
                binding.etDrugSearch.setText(text)
                binding.etDrugSearch.setSelection(text.length)
            }
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupConfirmButton() {
        // 확인 버튼 표시/숨김 및 클릭 처리
        updateConfirmButton()

        binding.btnConfirm?.setOnClickListener {
            if (selectedMedicines.isNotEmpty()) {
                navigateToDrugInfoInput()
            }
        }
    }

    private fun setupSelectedListButton() {
        binding.btnSelectedList?.setOnClickListener {
            if (selectedMedicines.isNotEmpty()) {
                showSelectedMedicinesBottomSheet()
            }
        }
    }

    private fun observeSearchResults() {
        viewModel.searchResults.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
    }

    private fun onMedicineSelected(medicine: MedicineSearchItem) {
        if (selectedMedicines.contains(medicine)) {
            selectedMedicines.remove(medicine)
            Toast.makeText(
                requireContext(),
                "${medicine.medicineName}" + getString(R.string.medicine_removed),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            selectedMedicines.add(medicine)
            Toast.makeText(
                requireContext(),
                "${medicine.medicineName}" + getString(R.string.medicine_added) + " (${selectedMedicines.size}개)",
                Toast.LENGTH_SHORT
            ).show()
        }

        updateConfirmButton()
    }

    private fun updateConfirmButton() {
        binding.btnConfirm?.let { button ->
            if (selectedMedicines.isEmpty()) {
                button.visibility = View.GONE
                binding.btnSelectedList?.visibility = View.GONE
            } else {
                button.visibility = View.VISIBLE
                button.text = getString(R.string.selection_complete) + "${selectedMedicines.size}" + getString(R.string.medicine_added_count)
                binding.btnSelectedList?.visibility = View.VISIBLE
            }
        }
    }

    private fun showSelectedMedicinesBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_selected_medicines, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rvSelectedMedicines)
        val emptyView = dialogView.findViewById<TextView>(R.id.tvEmptySelectedMedicines)

        val adapter = SelectedMedicineAdapter { medicine ->
            if (selectedMedicines.remove(medicine)) {
                updateConfirmButton()
                adapter.submitList(selectedMedicines.toList())
                emptyView.visibility = if (selectedMedicines.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        adapter.submitList(selectedMedicines.toList())

        emptyView.visibility = if (selectedMedicines.isEmpty()) View.VISIBLE else View.GONE

        dialog.setContentView(dialogView)
        dialog.show()
    }

    private fun navigateToDrugInfoInput() {
        val action = DrugSerchFragmentDirections.actionDrugSerchFragmentToDrugInfoInputFragment(
            selectedMedicines.toTypedArray()
        )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
