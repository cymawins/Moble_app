// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false

// 이 프로젝트에서 kotlin-android 플러그 인을 쓸 수 있다고 선언
    alias(libs.plugins.kotlin.android) apply false

// 이 프로젝트에서 kotlinx.serialization을 쓸 수 있다고 선언만 해둠 (실제 적용은 app 모듈에서)
    alias(libs.plugins.kotlin.serialization) apply false

// Hilt를 쓸 수 있다고 선언만 해둠 (실제 적용은 app 모듈에서)
    alias(libs.plugins.hilt.android) apply false
}