package com.kiuda.app.presentation.growth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiuda.app.domain.model.*
import com.kiuda.app.domain.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GrowthLogViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GrowthLogUiState>(GrowthLogUiState.Loading)
    val uiState: StateFlow<GrowthLogUiState> = _uiState.asStateFlow()

    private var allPlants: List<UserPlant> = emptyList()
    private var allRecords: List<GrowthRecordItem> = emptyList()
    private var currentFilterPlantId: Long? = null

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = GrowthLogUiState.Loading

            val plantsDeferred = async { dashboardRepository.getMyPlants() }
            val checklistDeferred = async { dashboardRepository.getChecklist() }

            val plants = plantsDeferred.await().getOrDefault(emptyList())
            val checklist = checklistDeferred.await().getOrDefault(emptyList())

            allPlants = if (plants.isEmpty()) {
                listOf(
                    UserPlant(1L, "초록이", "몬스테라", "거실 창가", null, "거실", "2024-01-15"),
                    UserPlant(2L, "싱싱이", "스파티필름", "침실", null, "침실", "2024-02-01"),
                    UserPlant(3L, "토마티", "방울토마토", "베란다", null, "베란다", "2024-03-10")
                )
            } else plants

            // 기존 체크리스트 및 데모 생육 이력 매핑
            val recordsFromChecklist = checklist.mapIndexed { idx, item ->
                val plant = allPlants.firstOrNull { it.id == item.plantId } ?: allPlants.firstOrNull()
                GrowthRecordItem(
                    id = item.id ?: (100L + idx),
                    plantId = plant?.id,
                    plantName = plant?.nickname ?: plant?.name ?: "내 식물",
                    plantType = plant?.plantType ?: "관엽식물",
                    recordType = when {
                        item.title?.contains("물") == true -> GrowthRecordType.WATERING
                        item.title?.contains("잎") == true || item.title?.contains("환기") == true -> GrowthRecordType.PEST_CHECK
                        item.title?.contains("영양") == true -> GrowthRecordType.FERTILIZER
                        else -> GrowthRecordType.NOTE
                    },
                    title = item.title ?: "관리 체크 완료",
                    content = if (item.completed) "오늘의 케어 루틴을 성공적으로 실행했습니다." else "오늘 완료할 케어 항목입니다.",
                    date = item.dueDate ?: "2026.08.27",
                    displayDate = if (item.dueDate == "오늘") "오늘 09:30" else item.dueDate ?: "오늘",
                    isCompleted = item.completed
                )
            }

            // 풍부한 데모 생육 일지 데이터 생성 (1차 요구사항 지원)
            val demoGrowthRecords = listOf(
                GrowthRecordItem(
                    id = 1L,
                    plantId = 1L,
                    plantName = "초록이",
                    plantType = "몬스테라",
                    recordType = GrowthRecordType.WATERING,
                    title = "아침 정기 관수 및 잎 먼지 케어",
                    content = "토양 수분 센서 55% 확인 후 500ml 흠뻑 관수했습니다. 잎 겉면 미온수로 가볍게 닦음.",
                    date = "2026.08.27",
                    displayDate = "오늘 09:30",
                    isCompleted = true
                ),
                GrowthRecordItem(
                    id = 2L,
                    plantId = 3L,
                    plantName = "토마티",
                    plantType = "방울토마토",
                    recordType = GrowthRecordType.PRUNING,
                    title = "3주차 곁순 제거 및 지주대 고정",
                    content = "원줄기와 잎자루 사이에 자란 곁순 3개를 제거하고, 열매 무게를 견디도록 지주대를 추가 고정함.",
                    date = "2026.08.26",
                    displayDate = "어제 16:20",
                    isCompleted = true
                ),
                GrowthRecordItem(
                    id = 3L,
                    plantId = 3L,
                    plantName = "토마티",
                    plantType = "방울토마토",
                    recordType = GrowthRecordType.BLOOM,
                    title = "첫 노란 꽃봉오리 확인 🌸",
                    content = "2화방에서 노란 꽃봉오리가 4개 착과되었습니다. 인공 수분을 위해 줄기를 살짝 흔들어줌.",
                    date = "2026.08.24",
                    displayDate = "8월 24일",
                    isCompleted = true
                ),
                GrowthRecordItem(
                    id = 4L,
                    plantId = 2L,
                    plantName = "싱싱이",
                    plantType = "스파티필름",
                    recordType = GrowthRecordType.FERTILIZER,
                    title = "액체 영양제 1000:1 희석 공급",
                    content = "생육 왕성기를 맞아 액체 관엽용 영양제를 물에 희석하여 뿌리 부근에 투여함.",
                    date = "2026.08.22",
                    displayDate = "8월 22일",
                    isCompleted = true
                ),
                GrowthRecordItem(
                    id = 5L,
                    plantId = 1L,
                    plantName = "초록이",
                    plantType = "몬스테라",
                    recordType = GrowthRecordType.PEST_CHECK,
                    title = "온새미 AI 잎 상태 정밀 진단",
                    content = "잎 표면 점무늬 유무를 촬영하여 AI 진단을 진행함. 병해충 없음(정상) 판정.",
                    date = "2026.08.20",
                    displayDate = "8월 20일",
                    isCompleted = true
                )
            )

            allRecords = (recordsFromChecklist + demoGrowthRecords).distinctBy { it.id }
            applyFilter()
        }
    }

    fun selectPlantFilter(plantId: Long?) {
        currentFilterPlantId = plantId
        applyFilter()
    }

    private fun applyFilter() {
        val filteredRecords = if (currentFilterPlantId == null) {
            allRecords
        } else {
            allRecords.filter { it.plantId == currentFilterPlantId }
        }

        val stats = GrowthSummaryStats(
            weeklyWateringCount = allRecords.count { it.recordType == GrowthRecordType.WATERING },
            totalActivePlants = allPlants.size,
            streakDays = 12
        )

        _uiState.value = GrowthLogUiState.Success(
            plants = allPlants,
            records = filteredRecords,
            selectedPlantId = currentFilterPlantId,
            stats = stats
        )
    }
}
