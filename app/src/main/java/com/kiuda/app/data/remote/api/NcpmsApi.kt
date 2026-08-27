package com.kiuda.app.data.remote.api

import com.kiuda.app.domain.model.*
import retrofit2.http.*

interface NcpmsApi {
    @GET("ncpms/alerts")
    suspend fun getAlerts(@Query("crop") crop: String? = null): NcpmsAlertListResponse

    @GET("ncpms/encyclopedia")
    suspend fun getEncyclopedia(
        @Query("q") q: String? = null,
        @Query("crop") crop: String? = null
    ): NcpmsEncyclopediaListResponse

    @GET("ncpms/encyclopedia/{id}")
    suspend fun getEncyclopediaDetail(@Path("id") id: String): NcpmsEncyclopediaItem

    @GET("ncpms/match")
    suspend fun matchEncyclopedia(@Query("name") name: String): NcpmsMatchResponse
}
