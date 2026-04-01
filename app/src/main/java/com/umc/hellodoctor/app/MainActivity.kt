package com.umc.hellodoctor.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.umc.hellodoctor.R
import com.umc.hellodoctor.core.network.status.NetworkViewModel
import com.umc.hellodoctor.core.permission.PermissionManager
import com.umc.hellodoctor.databinding.ActivityMainBinding
import com.umc.hellodoctor.feature.auth.domain.model.SocialSignInResult
import com.umc.hellodoctor.feature.auth.domain.repository.GoogleAuthServiceImpl
import com.umc.hellodoctor.feature.auth.presentation.AuthViewModel
import com.umc.hellodoctor.feature.language.presentation.LanguageManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var permissionManager: PermissionManager
    private val authViewModel: AuthViewModel by viewModels()
    private val networkViewModel: NetworkViewModel by viewModels()
    private lateinit var googleAuthService: GoogleAuthServiceImpl
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var hasHandledLoginResult: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 앱 시작 시 저장된 언어 설정 적용
        val languageManager = LanguageManager(this)
        languageManager.applyLanguage()

        enableEdgeToEdge()

        // Google Auth Service 초기화
        googleAuthService = GoogleAuthServiceImpl(this)

        permissionManager =
            PermissionManager(
                context = this,
                caller = this,
            )
        // 앱 실행 시 기본 권한 요청
        permissionManager.requestInitialPermissions(
            callback =
                object : PermissionManager.Callback {
                    override fun onPermissionsResult(
                        granted: List<String>,
                        denied: List<String>,
                    ) {
                        // 필요 없으면 비워둠
                    }
                },
        )
        // 사용해야 networkViewmodel활성화됨
        networkViewModel
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment =
            supportFragmentManager
                .findFragmentById(R.id.fragmentContainerView) as NavHostFragment

        navController = navHostFragment.navController

        // 앱 시작 시 자동 로그인
        performAutoSignIn()
        observeAuthState()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Google 로그인으로 자동 로그인 수행
     */
    private fun performAutoSignIn() {
        lifecycleScope.launch {
            authViewModel.socialLogin(googleAuthService)
        }
    }

    private fun observeAuthState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.uiState.collect { state ->
                    if (hasHandledLoginResult) return@collect
                    when (state.signInResult) {
                        is SocialSignInResult.Success -> {
                            Toast.makeText(this@MainActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                            if (state.isNewUser == true) {
                                navController.setGraph(R.navigation.nav_auth)
                                navController.navigate(R.id.userInfoFragment2)
                            } else {
                                navController.setGraph(R.navigation.nav_main)
                            }
                            hasHandledLoginResult = true
                        }
                        is SocialSignInResult.Canceled, is SocialSignInResult.Error -> {
                            navController.setGraph(R.navigation.nav_auth)
                        }
                        null -> Unit
                    }

                    // 프로필 생성 완료 시 메인 화면으로 이동
                    if (state.isProfileCreated && state.isNewUser == true) {
                        navController.setGraph(R.navigation.nav_main)
                    }
                }
            }
        }
    }
}
