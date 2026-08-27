package com.kiuda.app.presentation.auth.signup

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kiuda.app.R
import com.kiuda.app.databinding.ActivitySignupBinding
import com.kiuda.app.presentation.auth.login.LoginActivity
import com.kiuda.app.presentation.dashboard.DashboardActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val viewModel: SignupViewModel by viewModels()
    private var step = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (step == 2) {
                    goStep1()
                } else {
                    finish()
                }
            }
        })

        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.tvGoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnNext.setOnClickListener { goStep2() }
        binding.btnPrev.setOnClickListener { goStep1() }
        binding.btnSignup.setOnClickListener { doSignup() }

        binding.etPassword.addTextChangedListener(simpleWatcher { updateStrength() })
        binding.etPasswordConfirm.addTextChangedListener(simpleWatcher { updateMatch() })

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is SignupUiState.Idle -> {
                            binding.progress.visibility = View.GONE
                            binding.btnSignup.isEnabled = true
                        }
                        is SignupUiState.Loading -> {
                            binding.progress.visibility = View.VISIBLE
                            binding.btnSignup.isEnabled = false
                        }
                        is SignupUiState.Success -> {
                            binding.progress.visibility = View.GONE
                            Toast.makeText(this@SignupActivity, "환영합니다! 가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@SignupActivity, DashboardActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            })
                            finish()
                        }
                        is SignupUiState.Error -> {
                            binding.progress.visibility = View.GONE
                            binding.btnSignup.isEnabled = true
                            Toast.makeText(this@SignupActivity, state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun goStep2() {
        val email = binding.etEmail.text?.toString().orEmpty().trim()
        val pwd = binding.etPassword.text?.toString().orEmpty()
        val confirm = binding.etPasswordConfirm.text?.toString().orEmpty()
        if (email.isBlank()) {
            Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        if (pwd.length < 8) {
            Toast.makeText(this, "비밀번호는 8자 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        if (pwd != confirm) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        step = 2
        binding.panelStep1.visibility = View.GONE
        binding.panelStep2.visibility = View.VISIBLE
        binding.tvStepTitle.text = "프로필을 완성하고 가입을 완료하세요"
        binding.tvStepSub.text = "이름과 약관 동의만 남았어요"
        binding.step2Badge.setBackgroundResource(R.drawable.bg_step_active)
        binding.step2Badge.setTextColor(Color.WHITE)
    }

    private fun goStep1() {
        step = 1
        binding.panelStep2.visibility = View.GONE
        binding.panelStep1.visibility = View.VISIBLE
        binding.tvStepTitle.text = "이메일과 비밀번호를 입력해주세요"
        binding.tvStepSub.text = "키:우다와 함께하는 초록빛 일상의 시작"
        binding.step2Badge.setBackgroundResource(R.drawable.bg_step_inactive)
        binding.step2Badge.setTextColor(ContextCompat.getColor(this, R.color.kiuda_on_secondary_container))
    }

    private fun doSignup() {
        if (!binding.cbTerms.isChecked) {
            Toast.makeText(this, "이용약관에 동의해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        val email = binding.etEmail.text?.toString().orEmpty()
        val pwd = binding.etPassword.text?.toString().orEmpty()
        val name = binding.etName.text?.toString().orEmpty()
        viewModel.register(email, pwd, name, null, null, binding.cbMarketing.isChecked)
    }

    private fun updateStrength() {
        val pwd = binding.etPassword.text?.toString().orEmpty()
        val s = if (pwd.length >= 10 && pwd.any { it.isDigit() }) 3 else if (pwd.length >= 8) 2 else if (pwd.isNotEmpty()) 1 else 0
        val active = ContextCompat.getColor(this, R.color.kiuda_primary)
        val inactive = ContextCompat.getColor(this, R.color.kiuda_outline)
        binding.strength1.setBackgroundColor(if (s >= 1) active else inactive)
        binding.strength2.setBackgroundColor(if (s >= 2) active else inactive)
        binding.strength3.setBackgroundColor(if (s >= 3) active else inactive)
        binding.tvStrength.text = when (s) {
            0 -> "비밀번호 강도"
            1 -> "약함"
            2 -> "보통"
            else -> "강함"
        }
    }

    private fun updateMatch() {
        val pwd = binding.etPassword.text?.toString().orEmpty()
        val confirm = binding.etPasswordConfirm.text?.toString().orEmpty()
        if (confirm.isEmpty()) {
            binding.tvMatch.visibility = View.GONE
            return
        }
        binding.tvMatch.visibility = View.VISIBLE
        if (pwd == confirm) {
            binding.tvMatch.text = "비밀번호가 일치합니다"
            binding.tvMatch.setTextColor(ContextCompat.getColor(this, R.color.kiuda_success))
        } else {
            binding.tvMatch.text = "비밀번호가 일치하지 않습니다"
            binding.tvMatch.setTextColor(ContextCompat.getColor(this, R.color.kiuda_error))
        }
    }

    private fun simpleWatcher(onChange: () -> Unit) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) { onChange() }
    }
}