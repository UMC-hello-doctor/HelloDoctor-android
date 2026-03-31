package com.umc.hellodoctor.core.network.status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NetworkViewModel
    @Inject
    constructor(
        networkMonitor: NetworkMonitor,
    ) : ViewModel() {
        private val _isConnected = MutableStateFlow(true)
        val isConnected: StateFlow<Boolean> = _isConnected

        private val _status = MutableStateFlow(NetworkStatus.Available)
        val status: StateFlow<NetworkStatus> = _status

        init {
            viewModelScope.launch {
                networkMonitor.networkStatus.collect { state ->
                    _status.value = state
                    _isConnected.value = (state == NetworkStatus.Available)
                }
            }
        }
    }
