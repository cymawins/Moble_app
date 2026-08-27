package com.kiuda.app.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiuda.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashNavigationEvent {
    object NavigateToOnboarding : SplashNavigationEvent
    object NavigateToMain : SplashNavigationEvent
    object NavigateToLogin : SplashNavigationEvent
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<SplashNavigationEvent>()
    val navigationEvent: SharedFlow<SplashNavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        checkAuthStateAndNavigate()
    }

    private fun checkAuthStateAndNavigate() {
        viewModelScope.launch {
            // 웹 통합 브랜드 인상을 전달하기 위해 최소 1.2초 스플래시 노출 유지
            val splashDuration = 1200L
            val startTime = System.currentTimeMillis()

            val isOnboardingCompleted = authRepository.isOnboardingCompleted()
            val isLoggedIn = authRepository.isLoggedIn()

            val elapsedTime = System.currentTimeMillis() - startTime
            val remainingDelay = splashDuration - elapsedTime
            if (remainingDelay > 0) {
                delay(remainingDelay)
            }

            if (!isOnboardingCompleted) {
                _navigationEvent.emit(SplashNavigationEvent.NavigateToOnboarding)
            } else if (isLoggedIn) {
                _navigationEvent.emit(SplashNavigationEvent.NavigateToMain)
            } else {
                _navigationEvent.emit(SplashNavigationEvent.NavigateToLogin)
            }
        }
    }
}
