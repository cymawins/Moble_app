package com.kiuda.app.domain.model

data class NcpmsAlert(
    val id: String? = null,
    val crop: String? = null,
    val name: String? = null,
    val level: String? = null,
    val region: String? = null,
    val period: String? = null,
    val summary: String? = null,
    val source: String? = null,
    val sickKey: String? = null
)

data class NcpmsAlertListResponse(
    val items: List<NcpmsAlert> = emptyList(),
    val updatedAt: String? = null,
    val source: String? = null
)

data class NcpmsEncyclopediaItem(
    val id: String? = null,
    val crop: String? = null,
    val category: String? = null,
    val name: String? = null,
    val scientificName: String? = null,
    val summary: String? = null,
    val symptoms: String? = null,
    val environment: String? = null,
    val control: List<String>? = null,
    val prevention: String? = null,
    val tags: List<String>? = null,
    val source: String? = null,
    val sickKey: String? = null,
    val thumbImg: String? = null
)

data class NcpmsEncyclopediaListResponse(
    val items: List<NcpmsEncyclopediaItem> = emptyList(),
    val total: Int? = null,
    val source: String? = null
)

data class NcpmsMatchResponse(
    val items: List<NcpmsEncyclopediaItem> = emptyList(),
    val query: String? = null
)
