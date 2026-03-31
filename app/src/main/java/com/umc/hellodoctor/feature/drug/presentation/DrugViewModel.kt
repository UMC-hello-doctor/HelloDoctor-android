package com.umc.hellodoctor.feature.drug.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.hellodoctor.feature.drug.data.api.MedicineApi
import com.umc.hellodoctor.feature.drug.data.model.MedicineSearchItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrugViewModel
    @Inject
    constructor(
        private val medicineApi: MedicineApi,
    ) : ViewModel() {
        private val tag = "DrugViewModel"
        private val _inputText = MutableLiveData("")
        val inputText: LiveData<String> get() = _inputText

        private val _searchResults = MutableLiveData<List<MedicineSearchItem>>(emptyList())
        val searchResults: LiveData<List<MedicineSearchItem>> get() = _searchResults

        private val _uiState = MutableLiveData<DrugSearchUiState>(DrugSearchUiState.Idle)
        val uiState: LiveData<DrugSearchUiState> get() = _uiState

        private var searchJob: Job? = null

        fun updateInputText(text: String) {
            if (_inputText.value != text) {
                _inputText.value = text
            }
        }

        fun searchMedicines(keyword: String) {
            val trimmed = keyword.trim()
            if (trimmed.isEmpty()) {
                searchJob?.cancel()
                _searchResults.value = emptyList()
                _uiState.value = DrugSearchUiState.Idle
                return
            }

            searchJob?.cancel()
            searchJob =
                viewModelScope.launch {
                    _uiState.value = DrugSearchUiState.Loading
                    delay(300)
                    try {
                        val response = medicineApi.searchMedicines(trimmed)
                        if (response.success) {
                            val items = response.result
                            _searchResults.value = items
                            Log.d(tag, "searchMedicines: Found ${items.size} items - $items")
                            _uiState.value =
                                if (items.isEmpty()) {
                                    DrugSearchUiState.Empty
                                } else {
                                    DrugSearchUiState.Success(items.size)
                                }
                        } else {
                            _uiState.value = DrugSearchUiState.Error(response.message)
                        }
                    } catch (e: Exception) {
                        _uiState.value = DrugSearchUiState.Error(e.message ?: "네트워크 오류")
                    }
                }
        }
    }

sealed class DrugSearchUiState {
    object Idle : DrugSearchUiState()

    object Loading : DrugSearchUiState()

    object Empty : DrugSearchUiState()

    data class Success(val count: Int) : DrugSearchUiState()

    data class Error(val message: String) : DrugSearchUiState()
}
