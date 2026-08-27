import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
    buildFeatures {
        viewBinding = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
}
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)   // Kotlin 컴파일러가 생성하는 바이트코드를 JVM 11 타겟으로 맞춤 (Java의 compileOptions와 동일하게)
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

    // 네트워킹
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)

    // 비동기처리
    implementation(libs.kotlinx.coroutines.android)

    // JSON 직렬화
    implementation(libs.kotlinx.serialization.json)

    // 인증 (Credential Manager)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.security.crypto)

    // UI 추가 컴포넌트 & 이미지 (Navigation, Coil, SwipeRefreshLayout, CoordinatorLayout, Fragment KTX)
    val navVersion = "2.7.7"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    implementation("io.coil-kt:coil:2.5.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // CameraX (16 KB Page Alignment 지원 1.4.1 버전)
    val cameraxVersion = "1.4.1"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
}