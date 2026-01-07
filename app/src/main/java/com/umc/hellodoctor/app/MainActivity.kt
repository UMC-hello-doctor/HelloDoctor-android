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
import com.umc.hellodoctor.databinding.ActivityMainBinding
import com.umc.hellodoctor.feature.auth.presentation.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val TAG = this.javaClass.simpleName
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var binding : ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
 
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment


        navController = navHostFragment.navController


        binding.toMain.setOnClickListener {
            navController.setGraph(R.navigation.nav_main)
        }
        binding.toAuth.setOnClickListener {
            navController.setGraph(R.navigation.nav_auth)
        }




        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}