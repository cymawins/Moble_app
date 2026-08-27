package com.kiuda.app.domain.repository

import com.kiuda.app.domain.model.*

interface DiagnosisRepository {
    suspend fun getSymptomTags(): Result<List<SymptomTag>>
    suspend fun uploadBase64(imageBase64: String): Result<Base64UploadResponse>
    suspend fun predictQuestions(request: PredictRequest): Result<PredictResponse>
    suspend fun requestDiagnosis(request: DiagnosisRequest): Result<DiagnosisResult>
    suspend fun getDiagnosisResult(id: Long): Result<DiagnosisResult>
}
