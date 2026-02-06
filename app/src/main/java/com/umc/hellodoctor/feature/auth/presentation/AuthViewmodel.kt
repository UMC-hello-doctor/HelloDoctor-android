package com.umc.hellodoctor.feature.auth.presentation

import android.media.session.MediaSession
import android.util.Log
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
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    private val TAG  = "AuthViewModel"
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
                            tokenManager.clearAllTokens()
                            Log.d(TAG, "socialLogin: $result")
                            val response = authRepository.loginWithSocial(result.user).result
                            result.user.idToken?.let { tokenManager.saveGoogleIdToken(it) }
                            tokenManager.saveAuthTokens(
                                accessToken = response.accessToken,
                                refreshToken = response.refreshToken
                            )
                            tokenManager.debugLog()
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                signInResult = result,
                                errorMessage = null
                            )

                        } catch (e: Exception) {
                            Log.e(TAG, "socialLogin: ${e.message}" )
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
                        Log.i(TAG, "socialLogin: Canceled")
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