package com.kiuda.app.domain.model

enum class AlertLevel {
    HIGH,    // 위험 / 경보 (Red)
    MEDIUM,  // 경고 / 주의보 (Amber)
    LOW      // 주의 / 정보 (Blue)
}

data class SensorPestAlertItem(
    val id: String,
    val plantName: String,
    val location: String?,
    val title: String,
    val level: AlertLevel,
    val metricsText: String,
    val description: String,
    val causeAndGuide: String,
    val prefilledQuestion: String
)
