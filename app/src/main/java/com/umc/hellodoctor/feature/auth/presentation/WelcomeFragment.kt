package com.umc.hellodoctor.feature.auth.presentation


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.umc.hellodoctor.core.util.showToast
import com.umc.hellodoctor.core.util.toast
import com.umc.hellodoctor.databinding.FragmentWelcomeBinding
import com.umc.hellodoctor.feature.auth.domain.model.SocialSignInResult
import com.umc.hellodoctor.feature.auth.domain.repository.AuthRepository
import com.umc.hellodoctor.feature.auth.domain.repository.AuthRepositoryImpl
import com.umc.hellodoctor.feature.auth.domain.repository.SocialAuthService
import com.umc.hellodoctor.feature.auth.domain.repository.GoogleAuthServiceImpl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WelcomeFragment : Fragment() {
    private val TAG = this.javaClass.simpleName
    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()

    private lateinit var googleAuthService: SocialAuthService
    private lateinit var authService: SocialAuthService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        authService = GoogleAuthServiceImpl(requireContext())
        googleAuthService = GoogleAuthServiceImpl(requireContext())

        binding.googleSignInButton.setOnClickListener {
            viewModel.socialLogin(googleAuthService)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { state ->
                // 에러 처리
                state.errorMessage?.let { msg ->
                }

                // 로그인 성공 / 취소 / 실패에 따른 UI 처리
                when (val result = state.signInResult) {
                    is SocialSignInResult.Success -> {
                        toast("로그인에 성공했습니다.")
                        // 네비게이션 등
                    }
                    is SocialSignInResult.Canceled -> {
                        toast("로그인이 취소되었습니다.")
                        // 취소 처리
                    }
                    is SocialSignInResult.Error -> {
                        val message = result.throwable.message ?: "로그인에 실패했습니다."
                        toast(message)
                        // 실패 처리
                    }
                    null -> Unit
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}