package com.kiuda.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Hilt에게 "이 앱의 DI 컨테이너를 여기서부터 시작해라"고 알려주는 표시
// 이 어노테이션이 없으면 프로젝트 어디에서도 @Inject/@HiltViewModel이 동작하지 않음
@HiltAndroidApp
// Application을 상속만 하고 내용은 비워둠 - 지금은 Hilt 초기화 트리거 역할로 충분
class KiudaApplication : Application ()