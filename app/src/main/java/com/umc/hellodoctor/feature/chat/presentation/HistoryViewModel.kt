package com.umc.hellodoctor.feature.chat.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.hellodoctor.feature.chat.domain.model.ChatSession
import com.umc.hellodoctor.feature.chat.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel
    @Inject
    constructor(
        private val chatRepository: ChatRepository,
    ) : ViewModel() {
        private val _sessions = MutableLiveData<List<ChatSession>>()
        val sessions: LiveData<List<ChatSession>> = _sessions

        fun loadSessions() {
            viewModelScope.launch(Dispatchers.IO) {
                val result = chatRepository.getAllSessions()
                withContext(Dispatchers.Main) {
                    _sessions.value = result
                }
            }
        }
    }
