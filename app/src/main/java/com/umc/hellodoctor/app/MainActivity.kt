package com.umc.hellodoctor.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.umc.hellodoctor.R
import com.umc.hellodoctor.core.network.status.NetworkViewModel
import com.umc.hellodoctor.core.permission.PermissionManager
import com.umc.hellodoctor.databinding.ActivityMainBinding
import com.umc.hellodoctor.feature.auth.presentation.AuthViewModel
import com.umc.hellodoctor.core.location.LocationMapViewModel
import com.umc.hellodoctor.feature.language.presentation.LanguageManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val TAG = this.javaClass.simpleName
    private lateinit var permissionManager: PermissionManager
    private val authViewModel: AuthViewModel by viewModels()
    private val locationViewModel: LocationMapViewModel by viewModels()
    private val networkViewModel: NetworkViewModel by viewModels()

    private lateinit var binding : ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 앱 시작 시 저장된 언어 설정 적용
        val languageManager = LanguageManager(this)
        languageManager.applyLanguage()

        enableEdgeToEdge()

        permissionManager = PermissionManager(
            context = this,
            caller = this
        )
        // 앱 실행 시 기본 권한 요청
        permissionManager.requestInitialPermissions(callback = object : PermissionManager.Callback {
            override fun onPermissionsResult(
                granted: List<String>,
                denied: List<String>
            ) {
                // 필요 없으면 비워둠
            }
        })
        //사용해야 networkViewmodel활성화됨 
        networkViewModel
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment


        navController = navHostFragment.navController


        binding.toMain.setOnClickListener {
            binding.toMain.isSelected = true
            binding.toAuth.isSelected = false
            navController.setGraph(R.navigation.nav_main)
        }
        binding.toAuth.setOnClickListener {
            binding.toMain.isSelected = false
            binding.toAuth.isSelected = true
            navController.setGraph(R.navigation.nav_auth)
        }




        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}