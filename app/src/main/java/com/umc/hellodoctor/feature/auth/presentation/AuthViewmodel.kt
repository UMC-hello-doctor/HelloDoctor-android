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
    val errorMessage: String? = null,
    val isNewUser: Boolean? = null,
    val isProfileCreated: Boolean = false
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
                signInResult = null,
                isNewUser = null
            )

            try {
                when (val result = service.signIn()) {

                    is SocialSignInResult.Success -> {
                        try {
                            tokenManager.clearAllTokens()
                            Log.d(TAG, "socialLogin: $result")
                            val response = authRepository.loginWithSocial(result.user).result
                            Log.d(TAG, "socialLogin성공: $response")
                            result.user.idToken?.let { tokenManager.saveGoogleIdToken(it) }
                            tokenManager.saveAuthTokens(
                                accessToken = response.accessToken,
                                refreshToken = response.refreshToken
                            )
                            tokenManager.debugLog()
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                signInResult = result,
                                errorMessage = null,
                                isNewUser = response.isNewUser
                            )

                        } catch (e: Exception) {
                            Log.e(TAG, "socialLogin: ${e.message}" )
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                signInResult = null,
                                errorMessage = e.message ?: "서버 통신 중 오류가 발생했습니다.",
                                isNewUser = null
                            )
                        }
                    }
                    is SocialSignInResult.Canceled -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            signInResult = result,
                            errorMessage = null,
                            isNewUser = null
                        )
                        Log.i(TAG, "socialLogin: Canceled")
                    }
                    is SocialSignInResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            signInResult = null,
                            errorMessage = result.throwable.message ?: "소셜 로그인 중 오류가 발생했습니다.",
                            isNewUser = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    signInResult = null,
                    errorMessage = e.message ?: "소셜 로그인 실패",
                    isNewUser = null
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSignInResult() {
        _uiState.value = _uiState.value.copy(signInResult = null, isNewUser = null)
    }

    fun signOut(service: SocialAuthService? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                signInResult = null,
                isNewUser = null,
                isProfileCreated = false
            )
            try {
                service?.signOut()
            } catch (e: Exception) {
                Log.e(TAG, "signOut: ${e.message}")
            } finally {
                tokenManager.clearAllTokens()
                _uiState.value = AuthUiState()
            }
        }
    }

    /**
     * 프로필 생성 완료 상태 업데이트
     * UserInfoFragment에서 프로필 생성 완료 시 호출
     */
    fun onProfileCreated() {
        _uiState.value = _uiState.value.copy(isProfileCreated = true)
    }
}