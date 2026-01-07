package com.umc.hellodoctor.feature.auth.presentation

import com.umc.hellodoctor.feature.auth.domain.model.SocialSignInResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.hellodoctor.feature.auth.domain.repository.AuthRepository
import com.umc.hellodoctor.feature.auth.domain.repository.SocialAuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val signInResult: SocialSignInResult? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun socialLogin(service: SocialAuthService) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                signInResult = null
            )

            try {
                when (val result = service.signIn()) {
                    is SocialSignInResult.Success -> {
                        try {
                            val response = authRepository.loginWithSocial(result.user)
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                signInResult = result,
                                errorMessage = null
                            )
                        } catch (e: Exception) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                signInResult = null,
                                errorMessage = e.message ?: "서버 통신 중 오류가 발생했습니다."
                            )
                        }
                    }
                    is SocialSignInResult.Canceled -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            signInResult = result,
                            errorMessage = null
                        )
                    }
                    is SocialSignInResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            signInResult = null,
                            errorMessage = result.throwable.message ?: "소셜 로그인 중 오류가 발생했습니다."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    signInResult = null,
                    errorMessage = e.message ?: "소셜 로그인 실패"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSignInResult() {
        _uiState.value = _uiState.value.copy(signInResult = null)
    }
}