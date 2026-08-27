package com.kiuda.app.presentation.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.kiuda.app.databinding.ActivityOnboardingBinding
import com.kiuda.app.presentation.auth.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private val viewModel: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupListeners()
        observeEvents()
    }

    private fun setupViewPager() {
        val adapter = OnboardingAdapter(viewModel.onboardingItems)
        binding.viewPager.adapter = adapter

        // TabLayout 닷 인디케이터 연동
        TabLayoutMediator(binding.tabIndicator, binding.viewPager) { _, _ -> }.attach()

        // 슬라이드 체인지 리스너
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val isLastPage = position == viewModel.onboardingItems.size - 1
                binding.btnNextOrStart.text = if (isLastPage) "시작하기" else "다음"
            }
        })
    }

    private fun setupListeners() {
        // 건너뛰기 터치 시 즉시 온보딩 완료 후 로그인으로 진입
        binding.btnSkip.setOnClickListener {
            viewModel.completeOnboarding()
        }

        // 다음 / 시작하기 버튼 터치
        binding.btnNextOrStart.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < viewModel.onboardingItems.size - 1) {
                binding.viewPager.currentItem = currentItem + 1
            } else {
                viewModel.completeOnboarding()
            }
        }
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect { event ->
                    when (event) {
                        is OnboardingEvent.NavigateToLogin -> navigateToLogin()
                    }
                }
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
