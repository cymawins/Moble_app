plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    id("kotlin-kapt")
    // 빌트인 Kotlin을 껐으니 다시 명시적으로 필요. Hilt/kapt/serialization은
    // 이 플러그인 기준으로 검증된 조합이라 안정적으로 동작함
}

android {
    namespace = "com.kiuda.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kiuda.app"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    implementation(libs.hilt.android) // Hilt 런타임 라이브러리 (어노테이션 구현체)
    kapt(libs.hilt.compiler)  // 빌드 시점 실제 DI 연결 코드를 자동 생성하는 어노테이션 프로세서

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}