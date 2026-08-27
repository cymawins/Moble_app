package com.kiuda.app.presentation.onboarding

import androidx.annotation.DrawableRes

data class OnboardingItem(
    val stepBadge: String,
    val title: String,
    val description: String,
    @DrawableRes val iconResId: Int
)
