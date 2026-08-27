package com.kiuda.app.data.repository

import com.kiuda.app.data.remote.api.DiagnosisApi
import com.kiuda.app.domain.model.*
import com.kiuda.app.domain.repository.DiagnosisRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiagnosisRepositoryImpl @Inject constructor(
    private val api: DiagnosisApi
) : DiagnosisRepository {

    override suspend fun getSymptomTags(): Result<List<SymptomTag>> {
        return runCatching {
            api.getSymptomTags()
        }.recover {
            listOf(
                SymptomTag(1L, "잎 변색 (노랗게 변함)", "잎"),
                SymptomTag(2L, "잎 끝 마름", "잎"),
                SymptomTag(3L, "반점 발생", "잎"),
                SymptomTag(4L, "줄기 무름", "줄기"),
                SymptomTag(5L, "벌레 관찰됨", "병해충")
            )
        }
    }

    override suspend fun uploadBase64(imageBase64: String): Result<Base64UploadResponse> {
        return runCatching {
            api.uploadBase64(Base64UploadRequest(imageBase64))
        }.recover {
            Base64UploadResponse("demo_url", "demo_key", imageBase64.length)
        }
    }

    override suspend fun predictQuestions(request: PredictRequest): Result<PredictResponse> {
        return runCatching {
            api.predictQuestions(request)
        }.recover {
            PredictResponse(
                questions = listOf(
                    PredictedQuestion(1L, "잎 뒷면에 점이나 실이 보이나요?", "응애"),
                    PredictedQuestion(2L, "흙이 과도하게 젖어 있나요?", "과습")
                ),
                summary = "사진 분석 결과, 과습 또는 응애 피해가 의심됩니다."
            )
        }
    }

    override suspend fun requestDiagnosis(request: DiagnosisRequest): Result<DiagnosisResult> {
        return runCatching {
            api.requestDiagnosis(request)
        }.recover {
            DiagnosisResult(
                id = 100L,
                diagnosisName = "몬스테라 잎마름병 / 응애 초기",
                confidence = 88.5,
                reason = "잎 가장자리의 노란 변색과 건조 증상이 응애 초기 피해 양상과 유사합니다.",
                managementMethods = listOf(
                    "식물 잎 뒷면을 물로 깨끗이 씻어내어 먼지와 응애를 제거하세요.",
                    "통풍이 잘 되는 장소로 이동시키고 미온수로 분무해 습도를 유지하세요.",
                    "심할 경우 난황유 또는 친환경 잎 세정제를 3-5일 간격으로 살포하세요."
                ),
                steps = listOf(
                    DiagnosisStep(1, "격리", "다른 식물로 전이되지 않도록 격리 조치합니다."),
                    DiagnosisStep(2, "세척", "샤워기를 이용해 잎 앞뒷면을 물로 세척합니다."),
                    DiagnosisStep(3, "환경 개선", "서늘하고 통풍이 잘 되는 반음지로 이동합니다.")
                ),
                imageUrl = request.imageUrl,
                greeting = "안녕하세요! 키:우다 AI 식물 의사입니다.",
                closing = "지속적인 관심으로 식물이 다시 건강해질 수 있어요!"
            )
        }
    }

    override suspend fun getDiagnosisResult(id: Long): Result<DiagnosisResult> {
        return runCatching {
            api.getDiagnosisResult(id)
        }.recover {
            DiagnosisResult(
                id = id,
                diagnosisName = "데모 식물 진단 결과",
                confidence = 90.0,
                reason = "서버 연결이 없을 때 제공되는 기본 데모 진단 결과입니다.",
                managementMethods = listOf("적절한 관수 및 통풍 유지"),
                steps = listOf(DiagnosisStep(1, "기본 케어", "충분한 햇빛 제공"))
            )
        }
    }
}
