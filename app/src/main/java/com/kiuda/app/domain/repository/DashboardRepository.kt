package com.kiuda.app.domain.repository

import com.kiuda.app.domain.model.CareChecklistItem
import com.kiuda.app.domain.model.PestRiskAlert
import com.kiuda.app.domain.model.UserPlant
import com.kiuda.app.domain.model.WeatherSnapshot

interface DashboardRepository {
    suspend fun getMyPlants(): Result<List<UserPlant>>
    suspend fun getChecklist(): Result<List<CareChecklistItem>>
    suspend fun completeChecklistItem(id: Long): Result<CareChecklistItem>
    suspend fun getWeather(): Result<WeatherSnapshot>
    suspend fun getPestAlerts(): Result<List<PestRiskAlert>>
}
