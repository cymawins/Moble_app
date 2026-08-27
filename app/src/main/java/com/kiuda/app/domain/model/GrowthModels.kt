package com.kiuda.app.domain.model

enum class GrowthRecordType(
    val icon: String,
    val label: String
) {
    WATERING("💧", "관수/물주기"),
    PEST_CHECK("🍃", "상태점검"),
    FERTILIZER("💊", "영양공급"),
    PRUNING("✂️", "순지르기"),
    BLOOM("🌸", "개화/결실"),
    NOTE("📝", "생육일지")
}

data class GrowthRecordItem(
    val id: Long,
    val plantId: Long? = null,
    val plantName: String,
    val plantType: String? = null,
    val recordType: GrowthRecordType,
    val title: String,
    val content: String? = null,
    val date: String,
    val displayDate: String,
    val imageUrl: String? = null,
    val isCompleted: Boolean = true
)

data class GrowthSummaryStats(
    val weeklyWateringCount: Int = 0,
    val totalActivePlants: Int = 0,
    val streakDays: Int = 0
)

sealed class GrowthLogUiState {
    object Loading : GrowthLogUiState()
    data class Success(
        val plants: List<UserPlant>,
        val records: List<GrowthRecordItem>,
        val selectedPlantId: Long? = null,
        val stats: GrowthSummaryStats
    ) : GrowthLogUiState()
    data class Error(val message: String) : GrowthLogUiState()
}
