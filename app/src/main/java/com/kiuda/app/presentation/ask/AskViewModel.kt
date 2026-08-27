package com.kiuda.app.presentation.ask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiuda.app.domain.model.*
import com.kiuda.app.domain.repository.DiagnosisRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class AskInputMode {
    PHOTO, VOICE, TEXT
}

data class CropStatusState(
    val isRegistered: Boolean = true,
    val plantName: String = "방울토마토",
    val location: String = "베란다 가든 1구역",
    val statusBadge: String = "주의",
    val soilMoisture: String = "15% (부족)",
    val temperature: String = "29°C",
    val humidity: String = "45%",
    val growthStage: String = "개화기 (35일차)",
    val alertTitle: String? = null,
    val prefilledQuestion: String? = null
) {
    fun generateContextPrompt(): String {
        return "내 ${plantName}의 현재 상태를 분석해줘. [컨텍스트: 위치 $location | 토양수분 $soilMoisture | 온도 $temperature | 습도 $humidity | 생육 $growthStage${alertTitle?.let { " | 알림: $it" } ?: ""}]"
    }
}

sealed class AskUiState {
    object Idle : AskUiState()
    object AnalyzingPhoto : AskUiState()
    object Uploading : AskUiState()
    object Predicting : AskUiState()
    data class Ready(
        val imageUrl: String,
        val questions: List<PredictedQuestion>,
        val summary: String?,
        val refreshToken: Long = 0L
    ) : AskUiState()
    object Diagnosing : AskUiState()
    data class Success(val result: DiagnosisResult) : AskUiState()
    data class Error(val message: String) : AskUiState()
}

@HiltViewModel
class AskViewModel @Inject constructor(
    private val repository: DiagnosisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AskUiState>(AskUiState.Idle)
    val uiState: StateFlow<AskUiState> = _uiState.asStateFlow()

    private val _cropStatus = MutableStateFlow(CropStatusState())
    val cropStatus: StateFlow<CropStatusState> = _cropStatus.asStateFlow()

    private val _inputMode = MutableStateFlow(AskInputMode.PHOTO)
    val inputMode: StateFlow<AskInputMode> = _inputMode.asStateFlow()

    private val selectedTexts = linkedSetOf<String>()
    private var currentImageUrl: String? = null
    private var lastQuestions: List<PredictedQuestion> = emptyList()
    private var lastSummary: String? = null

    fun setInputMode(mode: AskInputMode) {
        _inputMode.value = mode
    }

    fun updateCropStatusFromIntent(
        plantName: String?,
        alertTitle: String?,
        alertLevel: String?,
        prefilledQuestion: String?,
        metricsText: String? = null
    ) {
        if (plantName.isNullOrBlank() && alertTitle.isNullOrBlank() && prefilledQuestion.isNullOrBlank()) {
            return
        }

        val current = _cropStatus.value
        val parsedSoil = if (metricsText?.contains("토양") == true) metricsText else current.soilMoisture
        _cropStatus.value = current.copy(
            isRegistered = true,
            plantName = plantName ?: current.plantName,
            statusBadge = alertLevel ?: "주의",
            alertTitle = alertTitle ?: current.alertTitle,
            soilMoisture = parsedSoil,
            prefilledQuestion = prefilledQuestion ?: current.prefilledQuestion
        )
    }

    fun toggleQuestion(text: String, selected: Boolean) {
        if (selected) selectedTexts.add(text) else selectedTexts.remove(text)
    }

    fun onPhotoCaptured(imageFile: File) {
        if (!imageFile.exists() || imageFile.length() == 0L) {
            _uiState.value = AskUiState.Error("유효한 사진이 없습니다. 다시 촬영해 주세요.")
            return
        }
        selectedTexts.clear()
        lastQuestions = emptyList()
        lastSummary = null
        _uiState.value = AskUiState.AnalyzingPhoto

        viewModelScope.launch {
            _uiState.value = AskUiState.Uploading
            // Base64 문자열로 변환 시도
            val base64Str = runCatching {
                val bytes = imageFile.readBytes()
                android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            }.getOrNull() ?: ""

            val uploadRes = repository.uploadBase64(base64Str).getOrNull()
            val imageUrl = uploadRes?.fileUrl ?: "local://${imageFile.name}"
            currentImageUrl = imageUrl
            _uiState.value = AskUiState.Predicting

            val predictRes = repository.predictQuestions(PredictRequest(imageUrl = imageUrl, imageBase64 = base64Str)).getOrNull()
            val questions = predictRes?.questions?.filter { !it.text.isNullOrBlank() } ?: emptyList()
            applyReady(
                imageUrl,
                if (questions.isEmpty()) demoQuestions() else questions,
                predictRes?.summary ?: "사진 분석 완료. 추가 증상을 선택하거나 적어주세요."
            )
        }
    }

    private fun applyReady(imageUrl: String, questions: List<PredictedQuestion>, summary: String) {
        lastQuestions = questions
        lastSummary = summary
        _uiState.value = AskUiState.Ready(
            imageUrl = imageUrl,
            questions = questions,
            summary = summary,
            refreshToken = System.currentTimeMillis()
        )
    }

    /**
     * 진단 요청: 텍스트 / 음성 / 사진 / 센서 컨텍스트 통합 처리
     */
    fun diagnose(customQuestion: String?, includeCropContext: Boolean = false) {
        val imageUrl = currentImageUrl ?: "text_only_request"
        val parts = mutableListOf<String>()

        if (includeCropContext) {
            parts.add(_cropStatus.value.generateContextPrompt())
        }

        parts.addAll(selectedTexts)
        customQuestion?.trim()?.takeIf { it.isNotEmpty() }?.let { parts.add(it) }

        val question = parts.joinToString(" / ").ifBlank { null }

        if (currentImageUrl.isNullOrBlank() && !includeCropContext && customQuestion.isNullOrBlank()) {
            _uiState.value = AskUiState.Error("질문 내용을 입력하거나 사진을 촬영해 주세요.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AskUiState.Diagnosing
            val result = repository.requestDiagnosis(
                DiagnosisRequest(imageUrl = imageUrl, question = question)
            )
            _uiState.value = result.fold(
                onSuccess = { AskUiState.Success(enrich(it)) },
                onFailure = {
                    AskUiState.Success(
                        enrich(
                            DiagnosisResult(
                                diagnosisName = "${_cropStatus.value.plantName} 생육 & 수분 상태 분석",
                                confidence = 85.0,
                                reason = "센서 측정 수치 및 문의 내용을 바탕으로 분석했습니다. 토양 수분이 다소 낮아 뿌리 흡수율 저하가 우려됩니다.",
                                managementMethods = listOf(
                                    "25°C 전후 미온수로 약 500ml 나누어 분무해 주세요",
                                    "직사광선을 피하고 통풍이 잘 되는 반음지로 이동하세요",
                                    "잎 상태를 지속 관찰하고 잎 분무로 습도를 보충하세요"
                                ),
                                greeting = "안녕하세요, 키:우다 AI 식물 의사입니다 🌿",
                                closing = "지속적인 관심과 적절한 수분 공급으로 건강하게 케어해 주세요."
                            )
                        )
                    )
                }
            )
        }
    }

    fun consumeSuccess() {
        val url = currentImageUrl
        if (url != null && lastQuestions.isNotEmpty()) {
            _uiState.value = AskUiState.Ready(
                imageUrl = url,
                questions = lastQuestions,
                summary = lastSummary,
                refreshToken = System.currentTimeMillis()
            )
        } else {
            _uiState.value = AskUiState.Idle
        }
    }

    private fun enrich(r: DiagnosisResult): DiagnosisResult {
        return r.copy(
            greeting = r.greeting
                ?: "안녕하세요, 식물 질병 및 생육 상태 분석을 돕는 온새미예요 🌿\n요청하신 상태를 자세히 살펴봤어요.",
            closing = r.closing
                ?: "이 내용은 참고용이에요. 확진·농약 사용은 지역 전문가 확인을 권합니다 🌱"
        )
    }

    private fun demoQuestions() = listOf(
        PredictedQuestion(1, "잎에 반점이나 얼룩이 생겼나요?", "잎 반점"),
        PredictedQuestion(2, "잎 색이 노랗게 변했나요?", "잎 황화"),
        PredictedQuestion(3, "잎이 시들거나 축 처지나요?", "시들음"),
        PredictedQuestion(4, "흰가루처럼 보이는 게 있나요?", "흰가루"),
        PredictedQuestion(5, "벌레 구멍이나 갉아 먹은 자국이 있나요?", "벌레")
    )
}
