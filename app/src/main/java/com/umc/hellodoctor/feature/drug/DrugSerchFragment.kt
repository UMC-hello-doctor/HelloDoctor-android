package com.umc.hellodoctor.feature.drug

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.umc.hellodoctor.databinding.FragmentAddDrugBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DrugSerchFragment : Fragment() {

    private var _binding: FragmentAddDrugBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DrugViewModel by viewModels()
    private val adapter = DrugSearchAdapter()

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

        binding.rvDrugResults.adapter = adapter

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

        viewModel.searchResults.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
