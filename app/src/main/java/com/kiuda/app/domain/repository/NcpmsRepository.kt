package com.kiuda.app.domain.repository

import com.kiuda.app.domain.model.*

interface NcpmsRepository {
    suspend fun getAlerts(crop: String? = null): Result<NcpmsAlertListResponse>
    suspend fun getEncyclopedia(query: String? = null, crop: String? = null): Result<NcpmsEncyclopediaListResponse>
    suspend fun getEncyclopediaDetail(id: String): Result<NcpmsEncyclopediaItem>
    suspend fun matchEncyclopedia(name: String): Result<NcpmsMatchResponse>
}
