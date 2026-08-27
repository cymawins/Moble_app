package com.kiuda.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiuda.app.data.local.TokenStore
import com.kiuda.app.domain.model.DashboardData
import com.kiuda.app.domain.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val data: DashboardData) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    val greetingName: String
        get() = tokenStore.getUserName() ?: "키움이"

    fun load() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading

            val plantsDeferred = async { dashboardRepository.getMyPlants() }
            val checklistDeferred = async { dashboardRepository.getChecklist() }
            val weatherDeferred = async { dashboardRepository.getWeather() }
            val alertsDeferred = async { dashboardRepository.getPestAlerts() }

            val plants = plantsDeferred.await().getOrDefault(emptyList())
            val checklist = checklistDeferred.await().getOrDefault(emptyList())
            val weather = weatherDeferred.await().getOrNull()
            val alerts = alertsDeferred.await().getOrDefault(emptyList())

            val dashboardData = DashboardData(
                plants = plants,
                checklist = checklist,
                weather = weather,
                pestAlerts = alerts
            )

            _uiState.value = DashboardUiState.Success(dashboardData)
        }
    }

    fun completeChecklist(id: Long) {
        viewModelScope.launch {
            dashboardRepository.completeChecklistItem(id)
            load()
        }
    }

    fun logout() {
        tokenStore.clearTokens()
    }
}
