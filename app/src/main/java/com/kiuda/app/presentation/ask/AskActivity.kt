package com.kiuda.app.presentation.ask

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kiuda.app.R
import com.kiuda.app.databinding.ActivityAskBinding
import com.kiuda.app.domain.model.PredictedQuestion
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class AskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAskBinding
    private val viewModel: AskViewModel by viewModels()
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var cameraStarted = false
    private var lastRefreshToken = -1L

    private var speechRecognizer: SpeechRecognizer? = null

    // 카메라 권한 요청
    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
        else {
            Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_LONG).show()
            binding.tvStatus.text = "카메라 권한이 없어 촬영할 수 없어요."
        }
    }

    // 마이크(오디오) 권한 요청
    private val requestAudioPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startListeningVoice()
        else {
            Toast.makeText(this, "음성 인식을 위해 마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "🤖 AI 묻다 · 온새미"
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.kiuda_text_primary))
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        cameraExecutor = Executors.newSingleThreadExecutor()

        // 1. 외부 컨텍스트 수신 (센서 알림 -> Intent Extra)
        handleExternalIntent(intent)

        // 2. 모드 전환 UI 칩 바인딩
        setupModeSwitcher()

        // 3. 작물 상태 카드 이벤트 바인딩 ("이 상태로 AI에게 묻기")
        setupCropStatusCard()

        // 4. 사진 촬영 및 진단 버튼 연결
        binding.btnCapture.setOnClickListener { takePhoto() }
        binding.btnDiagnose.setOnClickListener {
            val custom = binding.etCustomQuestion.text?.toString()
            viewModel.diagnose(customQuestion = custom, includeCropContext = true)
        }

        // 5. 음성 모드 버튼 연결
        binding.btnVoiceRecord.setOnClickListener { checkAudioPermissionAndListen() }
        binding.btnVoiceDiagnose.setOnClickListener {
            val question = binding.etVoiceQuestion.text?.toString()
            if (!question.isNullOrBlank()) {
                viewModel.diagnose(customQuestion = question, includeCropContext = true)
            } else {
                Toast.makeText(this, "음성으로 질문할 내용을 먼저 말씀해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 6. 텍스트 모드 버튼 & 퀵 템플릿 연결
        setupTextMode()

        // 7. 카메라 준비
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) startCamera()
        else requestCameraPermission.launch(Manifest.permission.CAMERA)

        // 8. StateFlow 수집
        observeViewModel()
    }

    private fun handleExternalIntent(intent: Intent?) {
        if (intent == null) return
        val prefilledQuestion = intent.getStringExtra(EXTRA_PREFILLED_QUESTION)
        val plantName = intent.getStringExtra(EXTRA_PLANT_NAME)
        val alertTitle = intent.getStringExtra(EXTRA_ALERT_TITLE)
        val alertLevel = intent.getStringExtra(EXTRA_ALERT_LEVEL)
        val metricsText = intent.getStringExtra(EXTRA_METRICS_TEXT)

        viewModel.updateCropStatusFromIntent(
            plantName = plantName,
            alertTitle = alertTitle,
            alertLevel = alertLevel,
            prefilledQuestion = prefilledQuestion,
            metricsText = metricsText
        )

        if (!prefilledQuestion.isNullOrBlank()) {
            binding.etTextQuestion.setText(prefilledQuestion)
            binding.etCustomQuestion.setText(prefilledQuestion)
            binding.etVoiceQuestion.setText(prefilledQuestion)
        }
    }

    private fun setupModeSwitcher() {
        binding.chipGroupMode.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            when (checkedIds.first()) {
                R.id.chipModePhoto -> {
                    viewModel.setInputMode(AskInputMode.PHOTO)
                    binding.containerPhotoMode.visibility = View.VISIBLE
                    binding.containerVoiceMode.visibility = View.GONE
                    binding.containerTextMode.visibility = View.GONE
                }
                R.id.chipModeVoice -> {
                    viewModel.setInputMode(AskInputMode.VOICE)
                    binding.containerPhotoMode.visibility = View.GONE
                    binding.containerVoiceMode.visibility = View.VISIBLE
                    binding.containerTextMode.visibility = View.GONE
                }
                R.id.chipModeText -> {
                    viewModel.setInputMode(AskInputMode.TEXT)
                    binding.containerPhotoMode.visibility = View.GONE
                    binding.containerVoiceMode.visibility = View.GONE
                    binding.containerTextMode.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupCropStatusCard() {
        lifecycleScope.launch {
            viewModel.cropStatus.collect { status ->
                val card = binding.cardCropStatus
                if (status.isRegistered) {
                    card.layoutPlantInfo.visibility = View.VISIBLE
                    card.layoutNoPlant.visibility = View.GONE
                    card.tvPlantName.text = status.plantName
                    card.tvPlantLocation.text = status.location
                    card.tvStatusBadge.text = status.statusBadge
                    card.tvSoilMoisture.text = "💧 토양 수분: ${status.soilMoisture}"
                    card.tvTemperature.text = "🌡️ 온도: ${status.temperature}"
                    card.tvHumidity.text = "💨 상대습도: ${status.humidity}"
                    card.tvGrowthStage.text = "🌸 생육: ${status.growthStage}"
                } else {
                    card.layoutPlantInfo.visibility = View.GONE
                    card.layoutNoPlant.visibility = View.VISIBLE
                }

                // "이 상태로 AI에게 묻기" 클릭 시
                card.btnAskWithStatus.setOnClickListener {
                    val contextPrompt = status.generateContextPrompt()
                    binding.etTextQuestion.setText(contextPrompt)
                    viewModel.diagnose(customQuestion = contextPrompt, includeCropContext = true)
                }
            }
        }
    }

    private fun setupTextMode() {
        binding.chipQuick1.setOnClickListener {
            binding.etTextQuestion.setText("현재 토양 수분 상태에서 물 주는 적정 주기와 양을 알려줘.")
        }
        binding.chipQuick2.setOnClickListener {
            binding.etTextQuestion.setText("개화기 생육 단계에 적합한 추천 영양제와 투여 방법을 알려줘.")
        }
        binding.chipQuick3.setOnClickListener {
            binding.etTextQuestion.setText("현재 온도/습도 조건에서 발생하기 쉬운 병해충 예방법을 알려줘.")
        }
        binding.btnTextDiagnose.setOnClickListener {
            val question = binding.etTextQuestion.text?.toString()
            if (!question.isNullOrBlank()) {
                viewModel.diagnose(customQuestion = question, includeCropContext = true)
            } else {
                Toast.makeText(this, "질문 내용을 입력해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAudioPermissionAndListen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startListeningVoice()
        } else {
            requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startListeningVoice() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "이 기기에서는 음성 인식을 지원하지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        }

        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREA.toString())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "작물 상태나 질문을 말씀하세요...")
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                binding.tvVoiceStatus.text = "🎙️ 듣고 있어요... 말씀해 주세요"
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                binding.tvVoiceStatus.text = "🔄 음성을 텍스트로 변환 중..."
            }
            override fun onError(error: Int) {
                binding.tvVoiceStatus.text = "마이크 버튼을 누르고 다시 말씀하세요"
                Toast.makeText(this@AskActivity, "음성 인식 실패 ($error)", Toast.LENGTH_SHORT).show()
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    binding.etVoiceQuestion.setText(text)
                    binding.tvVoiceStatus.text = "✅ 음성 인식 완료! 아래 [질문하기]를 누르세요"
                } else {
                    binding.tvVoiceStatus.text = "인식된 음성이 없습니다."
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(recognizerIntent)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AskUiState.Idle -> {
                            binding.progress.visibility = View.GONE
                            binding.btnDiagnose.isEnabled = false
                            binding.btnCapture.isEnabled = true
                            clearPredictUi()
                            binding.tvStatus.text = "작물 잎을 화면 중앙에 맞춰 촬영해 주세요."
                        }
                        is AskUiState.AnalyzingPhoto -> {
                            binding.progress.visibility = View.VISIBLE
                            binding.btnDiagnose.isEnabled = false
                            binding.btnCapture.isEnabled = false
                            clearPredictUi()
                            binding.tvStatus.text = "온새미가 사진을 살펴보는 중이에요..."
                        }
                        is AskUiState.Uploading -> {
                            binding.progress.visibility = View.VISIBLE
                            binding.btnDiagnose.isEnabled = false
                            binding.btnCapture.isEnabled = false
                            clearPredictUi()
                            binding.tvStatus.text = "사진을 올리는 중이에요..."
                        }
                        is AskUiState.Predicting -> {
                            binding.progress.visibility = View.VISIBLE
                            binding.btnDiagnose.isEnabled = false
                            binding.btnCapture.isEnabled = false
                            clearPredictUi()
                            binding.tvStatus.text = "온새미가 예상 질문을 정리하고 있어요..."
                        }
                        is AskUiState.Ready -> {
                            binding.progress.visibility = View.GONE
                            binding.btnDiagnose.isEnabled = true
                            binding.btnCapture.isEnabled = true
                            binding.tvStatus.text = state.summary
                                ?: "해당되는 질문을 고르거나 직접 적어 주세요."
                            if (state.refreshToken != lastRefreshToken) {
                                lastRefreshToken = state.refreshToken
                                binding.etCustomQuestion.setText("")
                                showPredictUi(state.questions)
                            }
                        }
                        is AskUiState.Diagnosing -> {
                            binding.progress.visibility = View.VISIBLE
                            binding.btnDiagnose.isEnabled = false
                            binding.btnCapture.isEnabled = false
                            binding.tvStatus.text = "온새미가 진단 중이에요. 잠시만요 🌿"
                        }
                        is AskUiState.Success -> {
                            binding.progress.visibility = View.GONE
                            binding.btnDiagnose.isEnabled = true
                            binding.btnCapture.isEnabled = true
                            val i = Intent(this@AskActivity, DiagnosisResultActivity::class.java).apply {
                                putExtra(DiagnosisResultActivity.EXTRA_NAME, state.result.diagnosisName)
                                putExtra(DiagnosisResultActivity.EXTRA_CONFIDENCE, state.result.confidence ?: 0.0)
                                putExtra(DiagnosisResultActivity.EXTRA_REASON, state.result.reason)
                                putExtra(DiagnosisResultActivity.EXTRA_GREETING, state.result.greeting)
                                putExtra(DiagnosisResultActivity.EXTRA_CLOSING, state.result.closing)
                                putStringArrayListExtra(
                                    DiagnosisResultActivity.EXTRA_METHODS,
                                    ArrayList(state.result.managementMethods ?: emptyList())
                                )
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            }
                            startActivity(i)
                            viewModel.consumeSuccess()
                        }
                        is AskUiState.Error -> {
                            binding.progress.visibility = View.GONE
                            binding.btnDiagnose.isEnabled = false
                            binding.btnCapture.isEnabled = true
                            binding.tvStatus.text = state.message
                            Toast.makeText(this@AskActivity, state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun clearPredictUi() {
        binding.tvPredictTitle.visibility = View.GONE
        binding.tvPredictHint.visibility = View.GONE
        binding.containerSymptoms.visibility = View.GONE
        binding.tvCustomLabel.visibility = View.GONE
        binding.tilCustomQuestion.visibility = View.GONE
        binding.containerSymptoms.removeAllViews()
    }

    private fun showPredictUi(questions: List<PredictedQuestion>) {
        binding.tvPredictTitle.visibility = View.VISIBLE
        binding.tvPredictHint.visibility = View.VISIBLE
        binding.containerSymptoms.visibility = View.VISIBLE
        binding.tvCustomLabel.visibility = View.VISIBLE
        binding.tilCustomQuestion.visibility = View.VISIBLE
        binding.containerSymptoms.removeAllViews()
        val color = ContextCompat.getColor(this, R.color.kiuda_text_primary)
        questions.forEach { q ->
            val text = q.text ?: return@forEach
            val cb = CheckBox(this).apply {
                this.text = text
                textSize = 14f
                setTextColor(color)
                setPadding(8, 10, 8, 10)
                setOnCheckedChangeListener { _, checked ->
                    viewModel.toggleQuestion(text, checked)
                }
            }
            binding.containerSymptoms.addView(cb)
        }
    }

    private fun startCamera() {
        if (cameraStarted) return
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(binding.previewView.surfaceProvider)
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
                cameraStarted = true
                binding.tvStatus.text = "작물 잎을 화면 중앙에 맞춰 촬영해 주세요."
            } catch (e: Exception) {
                Toast.makeText(this, "카메라 시작 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.tvStatus.text = "카메라를 사용할 수 없어요."
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture
        if (imageCapture == null) {
            Toast.makeText(this, "카메라가 아직 준비되지 않았어요.", Toast.LENGTH_SHORT).show()
            return
        }
        clearPredictUi()
        binding.btnDiagnose.isEnabled = false
        binding.tvStatus.text = "촬영 중..."

        val photoFile = File(
            cacheDir,
            "kiuda_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA)
                .format(System.currentTimeMillis()) + ".jpg"
        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    binding.tvStatus.text = "촬영 완료 · 온새미가 분석해요"
                    viewModel.onPhotoCaptured(photoFile)
                }
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(this@AskActivity, "촬영 실패: ${exc.message}", Toast.LENGTH_SHORT).show()
                    binding.tvStatus.text = "다시 촬영해 주세요."
                    binding.btnCapture.isEnabled = true
                }
            }
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::cameraExecutor.isInitialized) cameraExecutor.shutdown()
        speechRecognizer?.destroy()
    }

    companion object {
        const val EXTRA_PREFILLED_QUESTION = "extra_prefilled_question"
        const val EXTRA_PLANT_NAME = "extra_plant_name"
        const val EXTRA_ALERT_TITLE = "extra_alert_title"
        const val EXTRA_ALERT_LEVEL = "extra_alert_level"
        const val EXTRA_METRICS_TEXT = "extra_metrics_text"
    }
}
