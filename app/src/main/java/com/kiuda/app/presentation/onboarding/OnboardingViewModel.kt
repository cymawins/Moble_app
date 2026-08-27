package com.kiuda.app.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiuda.app.R
import com.kiuda.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OnboardingEvent {
    object NavigateToLogin : OnboardingEvent
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _event = MutableSharedFlow<OnboardingEvent>()
    val event: SharedFlow<OnboardingEvent> = _event.asSharedFlow()

    val onboardingItems = listOf(
        OnboardingItem(
            stepBadge = "STEP 01. 보다",
            title = "내 작물 상태를 한눈에",
            description = "내 작물이 지금 어떤 상태인지\n한눈에 확인하고 정성껏 케어해보세요.",
            iconResId = R.drawable.ic_onboarding_look
        ),
        OnboardingItem(
            stepBadge = "STEP 02. 묻다",
            title = "AI에게 바로 물어보기",
            description = "궁금하거나 아픈 상태가 있을 땐\n사진 한 장으로 AI 온새미에게 물어보세요.",
            iconResId = R.drawable.ic_onboarding_ask
        ),
        OnboardingItem(
            stepBadge = "STEP 03. 함께",
            title = "이웃과 나누는 초록빛 일상",
            description = "가까운 이웃과 키움의 순간을 연결하고\n따뜻한 경험과 지식을 나누어보세요.",
            iconResId = R.drawable.ic_onboarding_together
        )
    )

    fun completeOnboarding() {
        viewModelScope.launch {
            authRepository.setOnboardingCompleted(true)
            _event.emit(OnboardingEvent.NavigateToLogin)
        }
    }
}
