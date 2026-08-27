package com.kiuda.app.presentation.auth.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kiuda.app.databinding.ActivityLoginBinding
import com.kiuda.app.presentation.dashboard.DashboardActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text?.toString().orEmpty()
            val password = binding.etPassword.text?.toString().orEmpty()
            viewModel.login(username, password)
        }

        binding.btnGoSignup?.setOnClickListener {
            startActivity(Intent(this, com.kiuda.app.presentation.auth.signup.SignupActivity::class.java))
        }

        binding.btnGoogleLogin.setOnClickListener {
            viewModel.loginWithGoogle(this)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is LoginUiState.Idle -> {
                            binding.progress.visibility = View.GONE
                            binding.btnLogin.isEnabled = true
                        }
                        is LoginUiState.Loading -> {
                            binding.progress.visibility = View.VISIBLE
                            binding.btnLogin.isEnabled = false
                        }
                        is LoginUiState.Success -> {
                            binding.progress.visibility = View.GONE
                            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            })
                            finish()
                        }
                        is LoginUiState.Error -> {
                            binding.progress.visibility = View.GONE
                            binding.btnLogin.isEnabled = true
                            Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}