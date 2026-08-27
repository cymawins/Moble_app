package com.kiuda.app.data.repository

import com.kiuda.app.data.remote.api.NcpmsApi
import com.kiuda.app.domain.model.*
import com.kiuda.app.domain.repository.NcpmsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NcpmsRepositoryImpl @Inject constructor(
    private val api: NcpmsApi
) : NcpmsRepository {

    override suspend fun getAlerts(crop: String?): Result<NcpmsAlertListResponse> {
        return runCatching {
            api.getAlerts(crop)
        }.recover {
            NcpmsAlertListResponse(
                items = listOf(
                    NcpmsAlert("1", "고추", "고추 탄저병 주의보", "HIGH", "전국", "7월~8월", "고온 다습한 날씨로 탄저병 예찰 필요", "농촌진흥청"),
                    NcpmsAlert("2", "토마토", "토마토 잎곰팡이병 경보", "HIGH", "시설재배지", "연중", "환기 부족 시 발병율 증가", "농촌진흥청")
                )
            )
        }
    }

    override suspend fun getEncyclopedia(query: String?, crop: String?): Result<NcpmsEncyclopediaListResponse> {
        return runCatching {
            api.getEncyclopedia(query, crop)
        }.recover {
            NcpmsEncyclopediaListResponse(
                items = listOf(
                    NcpmsEncyclopediaItem(
                        id = "E1",
                        crop = "몬스테라",
                        category = "병해",
                        name = "점무늬병",
                        scientificName = "Cercospora sp.",
                        summary = "잎에 갈색 또는 검은색 반점이 형성되는 질병",
                        symptoms = "잎 표면에 동심원 모양의 갈색 점이 생기며 점차 확대됨",
                        environment = "고온 다습하고 통풍이 불량한 환경",
                        control = listOf("병든 잎을 즉시 제거", "살균제 처리"),
                        prevention = "과습을 방지하고 통풍 유지",
                        tags = listOf("잎반점", "곰팡이")
                    )
                ),
                total = 1
            )
        }
    }

    override suspend fun getEncyclopediaDetail(id: String): Result<NcpmsEncyclopediaItem> {
        return runCatching {
            api.getEncyclopediaDetail(id)
        }.recover {
            NcpmsEncyclopediaItem(
                id = id,
                crop = "몬스테라",
                category = "병해",
                name = "점무늬병",
                scientificName = "Cercospora sp.",
                summary = "잎에 갈색 또는 검은색 반점이 형성되는 질병",
                symptoms = "잎 표면에 동심원 모양의 갈색 점이 생기며 점차 확대됩니다.",
                environment = "고온 다습하고 통풍이 불량한 환경에서 발생하기 쉽습니다.",
                control = listOf("병든 잎은 즉시 잘라내어 정돈합니다.", "적절한 친환경 살균제를 도포합니다."),
                prevention = "과습을 줄이고 바람이 잘 통하는 환경을 만듭니다."
            )
        }
    }

    override suspend fun matchEncyclopedia(name: String): Result<NcpmsMatchResponse> {
        return runCatching {
            api.matchEncyclopedia(name)
        }.recover {
            NcpmsMatchResponse(items = emptyList(), query = name)
        }
    }
}
