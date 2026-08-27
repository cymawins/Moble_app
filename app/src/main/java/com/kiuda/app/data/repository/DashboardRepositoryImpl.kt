package com.kiuda.app.data.repository

import com.kiuda.app.data.remote.api.DashboardApi
import com.kiuda.app.domain.model.*
import com.kiuda.app.domain.repository.DashboardRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val api: DashboardApi
) : DashboardRepository {

    override suspend fun getMyPlants(): Result<List<UserPlant>> {
        return runCatching {
            api.getMyPlants()
        }.recover {
            // 데모 폴백 데이터
            listOf(
                UserPlant(1L, "초록이", "몬스테라", "거실 창가", null, "거실", "2024-01-15"),
                UserPlant(2L, "싱싱이", "스파티필름", "침실", null, "침실", "2024-02-01")
            )
        }
    }

    override suspend fun getChecklist(): Result<List<CareChecklistItem>> {
        return runCatching {
            api.getChecklist()
        }.recover {
            listOf(
                CareChecklistItem(1L, "몬스테라 물주기", false, 1L, "오늘"),
                CareChecklistItem(2L, "잎 닦아주기", true, 1L, "어제"),
                CareChecklistItem(3L, "스파티필름 환기 시키기", false, 2L, "오늘")
            )
        }
    }

    override suspend fun completeChecklistItem(id: Long): Result<CareChecklistItem> {
        return runCatching {
            api.completeChecklistItem(id)
        }.recover {
            CareChecklistItem(id, "체크 완료 항목", true, 1L, "오늘")
        }
    }

    override suspend fun getWeather(): Result<WeatherSnapshot> {
        return runCatching {
            api.getWeather()
        }.recover {
            WeatherSnapshot(23.5, "맑음", 55, "서울")
        }
    }

    override suspend fun getPestAlerts(): Result<List<PestRiskAlert>> {
        return runCatching {
            api.getPestAlerts()
        }.recover {
            listOf(
                PestRiskAlert(1L, "응애 주의보", "HIGH", "건조한 날씨로 인해 응애 발생 위험이 높습니다."),
                PestRiskAlert(2L, "깍지벌레 예방", "MEDIUM", "통풍이 잘 되지 않는 환경을 주의하세요.")
            )
        }
    }
}
