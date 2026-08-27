package com.kiuda.app.domain.model

data class SymptomTag(
    val id: Long? = null,
    val name: String? = null,
    val category: String? = null
)

data class Base64UploadRequest(
    val imageBase64: String
)

data class Base64UploadResponse(
    val fileUrl: String? = null,
    val key: String? = null,
    val bytes: Int? = null
)

data class PresignedUrlRequest(
    val fileName: String,
    val contentType: String = "image/jpeg"
)

data class PresignedUrlResponse(
    val uploadUrl: String? = null,
    val fileUrl: String? = null,
    val key: String? = null
)

data class DiagnosisRequest(
    val imageUrl: String,
    val symptomTagIds: List<Long> = emptyList(),
    val plantId: Long? = null,
    val question: String? = null,
    val imageBase64: String? = null
)

data class DiagnosisStep(
    val step: Int? = null,
    val title: String? = null,
    val description: String? = null
)

data class PredictRequest(
    val imageUrl: String,
    val imageBase64: String? = null
)

data class PredictedQuestion(
    val id: Long? = null,
    val text: String? = null,
    val tag: String? = null
)

data class PredictResponse(
    val questions: List<PredictedQuestion> = emptyList(),
    val summary: String? = null
)

data class DiagnosisResult(
    val id: Long? = null,
    val diagnosisName: String? = null,
    val confidence: Double? = null,
    val reason: String? = null,
    val managementMethods: List<String>? = null,
    val steps: List<DiagnosisStep>? = null,
    val imageUrl: String? = null,
    val greeting: String? = null,
    val closing: String? = null,
    val provider: String? = null
)
