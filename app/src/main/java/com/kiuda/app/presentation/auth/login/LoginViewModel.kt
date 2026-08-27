package com.kiuda.app.presentation.auth.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiuda.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LoginUiState {
    // 화면이 가질 수 있는 상태를 전부 나열 - 화면(Activity)은 이중 뭐가 왔는지만 보고 UI를 바꾸면 됨
    object Idle : LoginUiState // 아무것도 안한 초기 상태
    object Loading : LoginUiState // 로그인 요청 중 (로딩 스피너 표시용)
    object Success : LoginUiState // 로그인 성공 (다음 화면 이동용)
    data class Error(val message: String): LoginUiState // 실패 (에러 메시지 표시용)
}

// Hilt가 이 ViewModel을 만들 때 AuthRepository를 자동으로 주입해주도록 표시
@HiltViewModel
class LoginViewModel @Inject constructor(
   private val authRepository: AuthRepository
) : ViewModel(){

    // ViewModel 내부에서만 값을 바꿀 수 있는 "쓰기"용 버전(private)
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    // 화면(Activity)에게는 이 "읽기 전용" 버전만 노출 - 화면이 실수로 상태를 직접 바꾸지 못하게 막음
    val uiState : StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email:String,password:String){
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("이메일과 비밀번호를 입력해주세요")
            return
        }
        viewModelScope.launch {
            // viewModelScope: 화면이 꺼지면 자동으로 취소되는 코루틴 범위 - 메모리 누수 방지
            _uiState.value = LoginUiState.Loading
            authRepository.login(email, password)
                .onSuccess { _uiState.value = LoginUiState.Success }
                .onFailure { e -> _uiState.value = LoginUiState.Error(e.message ?: "로그인에 실패했습니다.") }
        }
    }

    fun loginWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            authRepository.loginWithGoogle(activityContext)
                .onSuccess { _uiState.value = LoginUiState.Success }
                .onFailure { e -> _uiState.value = LoginUiState.Error(e.message ?: "구글 로그인에 실패했습니다.") }
        }
    }

}