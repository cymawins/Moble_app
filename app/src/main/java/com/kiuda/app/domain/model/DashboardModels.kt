package com.kiuda.app.domain.model

data class UserPlant(
    val id: Long? = null,
    val name: String? = null,
    val plantType: String? = null,
    val nickname: String? = null,
    val imageUrl: String? = null,
    val location: String? = null,
    val createdAt: String? = null
)

data class CareChecklistItem(
    val id: Long? = null,
    val title: String? = null,
    val completed: Boolean = false,
    val plantId: Long? = null,
    val dueDate: String? = null
)

data class WeatherSnapshot(
    val temperature: Double? = null,
    val condition: String? = null,
    val humidity: Int? = null,
    val location: String? = null
)

data class PestRiskAlert(
    val id: Long? = null,
    val title: String? = null,
    val level: String? = null, // LOW, MEDIUM, HIGH
    val description: String? = null
)

data class DashboardData(
    val plants: List<UserPlant> = emptyList(),
    val checklist: List<CareChecklistItem> = emptyList(),
    val weather: WeatherSnapshot? = null,
    val pestAlerts: List<PestRiskAlert> = emptyList()
)
