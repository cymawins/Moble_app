package com.kiuda.app.presentation.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiuda.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SignupUiState {
    object Idle : SignupUiState
    object Loading : SignupUiState
    object Success : SignupUiState
    data class Error(val message: String) : SignupUiState
}

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignupUiState>(SignupUiState.Idle)
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    fun register(
        email: String,
        password: String,
        name: String,
        province: String? = null,
        district: String? = null,
        marketingAgreed: Boolean = false
    ) {
        val trimmedEmail = email.trim()
        val trimmedName = name.trim()

        if (trimmedEmail.isBlank() || password.isBlank() || trimmedName.isBlank()) {
            _uiState.value = SignupUiState.Error("이메일, 비밀번호, 이름을 모두 입력해 주세요.")
            return
        }
        if (password.length < 8) {
            _uiState.value = SignupUiState.Error("비밀번호는 8자 이상이어야 합니다.")
            return
        }

        viewModelScope.launch {
            _uiState.value = SignupUiState.Loading
            authRepository.register(trimmedEmail, password, trimmedName, province, district, marketingAgreed)
                .onSuccess { _uiState.value = SignupUiState.Success }
                .onFailure { e -> _uiState.value = SignupUiState.Error(e.message ?: "회원가입에 실패했습니다.") }
        }
    }
}