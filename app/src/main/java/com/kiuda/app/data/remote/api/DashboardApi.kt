package com.kiuda.app.data.remote.api

import com.kiuda.app.domain.model.CareChecklistItem
import com.kiuda.app.domain.model.PestRiskAlert
import com.kiuda.app.domain.model.UserPlant
import com.kiuda.app.domain.model.WeatherSnapshot
import retrofit2.http.*

interface DashboardApi {
    @GET("plants")
    suspend fun getMyPlants(): List<UserPlant>

    @GET("plants/{id}")
    suspend fun getPlant(@Path("id") id: Long): UserPlant

    @GET("checklist")
    suspend fun getChecklist(): List<CareChecklistItem>

    @PUT("checklist/{id}/complete")
    suspend fun completeChecklistItem(@Path("id") id: Long): CareChecklistItem

    @GET("weather")
    suspend fun getWeather(): WeatherSnapshot

    @GET("pest-alerts")
    suspend fun getPestAlerts(): List<PestRiskAlert>
}
