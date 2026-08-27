package com.kiuda.app.data.remote.api

import com.kiuda.app.domain.model.*
import retrofit2.http.*

interface DiagnosisApi {
    @GET("symptoms")
    suspend fun getSymptomTags(): List<SymptomTag>

    @POST("upload/base64")
    suspend fun uploadBase64(@Body request: Base64UploadRequest): Base64UploadResponse

    @POST("upload/presigned")
    suspend fun getPresignedUrl(@Body request: PresignedUrlRequest): PresignedUrlResponse

    @POST("ai/predict")
    suspend fun predictQuestions(@Body request: PredictRequest): PredictResponse

    @POST("ai/diagnose")
    suspend fun requestDiagnosis(@Body request: DiagnosisRequest): DiagnosisResult

    @GET("ai/diagnose/{id}")
    suspend fun getDiagnosisResult(@Path("id") id: Long): DiagnosisResult
}
