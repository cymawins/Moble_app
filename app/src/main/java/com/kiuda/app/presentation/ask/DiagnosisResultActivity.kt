package com.kiuda.app.presentation.ask

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.kiuda.app.R
import com.kiuda.app.databinding.ActivityDiagnosisResultBinding
import com.kiuda.app.databinding.ItemDiagnosisMethodBinding
import com.kiuda.app.presentation.ncpms.EncyclopediaListActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiagnosisResultActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NAME = "diagnosisName"
        const val EXTRA_CONFIDENCE = "confidence"
        const val EXTRA_REASON = "reason"
        const val EXTRA_METHODS = "methods"
        const val EXTRA_GREETING = "greeting"
        const val EXTRA_CLOSING = "closing"
    }

    private lateinit var binding: ActivityDiagnosisResultBinding
    private var finishing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiagnosisResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 상단 앱바 설정
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "온새미 진단"
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.kiuda_text_primary))
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishOnce()
            }
        })

        // 2. Intent Extra 데이터 추출
        val name = intent.getStringExtra(EXTRA_NAME) ?: "진단명 없음"
        val confidence = intent.getDoubleExtra(EXTRA_CONFIDENCE, 0.0)
        val reason = intent.getStringExtra(EXTRA_REASON).orEmpty()
        val methods = intent.getStringArrayListExtra(EXTRA_METHODS) ?: arrayListOf()
        val greeting = intent.getStringExtra(EXTRA_GREETING)
            ?: "안녕하세요! 식물 질병 분석을 돕는 온새미예요"
        val closing = intent.getStringExtra(EXTRA_CLOSING)
            ?: "최종 확진은 전문가 확인을 권장해요. 궁금한 점이 있으면 언제든 다시 질문해 주세요 🌱"

        // 3. 인사말 및 진단명 설정
        binding.tvGreeting.text = sanitizeDisplay(greeting)
        binding.tvDiagnosisName.text = sanitizeDisplay(name)

        // 4. 확신도 (Confidence %) 및 프로그레스 애니메이션
        val percent = when {
            confidence <= 0.0 -> 0
            confidence <= 1.0 -> (confidence * 100).toInt()
            else -> confidence.toInt().coerceIn(0, 100)
        }
        binding.tvConfidence.text = "확신도  ${percent}%"

        // 확신도 수준 뱃지 설정
        val (badgeText, badgeColorRes) = when {
            percent >= 80 -> "높은 확신도" to R.color.kiuda_primary
            percent >= 50 -> "보통 확신도" to R.color.kiuda_secondary
            else -> "참고용 진단" to R.color.kiuda_tertiary
        }
        binding.tvConfidenceBadge.text = badgeText
        binding.tvConfidenceBadge.setTextColor(ContextCompat.getColor(this, badgeColorRes))

        // 부드러운 진행바 애니메이션
        ObjectAnimator.ofInt(binding.progressConfidence, "progress", 0, percent).apply {
            duration = 600
            interpolator = DecelerateInterpolator()
            start()
        }

        // 5. 온새미의 상세 분석 (이유)
        val cleanReason = sanitizeDisplay(reason)
        binding.tvReason.text = cleanReason.ifBlank {
            "자세한 분석 내용이 없습니다. 사진을 다른 각도에서 다시 촬영해 보세요."
        }

        // 6. 전문가 안내 닫기 문구
        binding.tvClosing.text = sanitizeDisplay(closing)

        // 7. 단계별 조치 가이드 (Item Binding 사용)
        binding.containerMethods.removeAllViews()
        val cleanMethods = methods.map { sanitizeDisplay(it) }.filter { it.isNotBlank() }

        if (cleanMethods.isEmpty()) {
            val emptyBinding = ItemDiagnosisMethodBinding.inflate(layoutInflater, binding.containerMethods, false)
            emptyBinding.tvStepNumber.text = "!"
            emptyBinding.tvStepText.text = "통풍과 물 주기부터 천천히 점검해 보세요."
            binding.containerMethods.addView(emptyBinding.root)
        } else {
            cleanMethods.forEachIndexed { index, method ->
                val methodBinding = ItemDiagnosisMethodBinding.inflate(layoutInflater, binding.containerMethods, false)
                methodBinding.tvStepNumber.text = "${index + 1}"
                methodBinding.tvStepText.text = method
                binding.containerMethods.addView(methodBinding.root)
            }
        }

        // 8. 하단 버튼 액션 바인딩
        binding.btnEncyclopedia.setOnClickListener {
            startActivity(Intent(this, EncyclopediaListActivity::class.java).apply {
                putExtra(EncyclopediaListActivity.EXTRA_QUERY, name)
            })
        }
        binding.btnClose.setOnClickListener { finishOnce() }
    }

    private fun sanitizeDisplay(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        var s = raw.trim()
        val looksJson = s.contains("\"diagnosisName\"") || s.contains("```") ||
            (s.startsWith("{") && s.contains("\"reason\""))
        if (looksJson) {
            Regex("\"reason\"\\s*:\\s*\"([\\s\\S]*?)\"").find(s)?.groupValues?.getOrNull(1)?.let {
                return it.replace("\\n", "\n").replace("\\\"", "\"")
            }
            s = s.replace(Regex("```[a-zA-Z]*"), "")
                .replace("```", "")
                .replace(Regex("[{}\\[\\]\"]"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
        }
        return s
    }

    private fun finishOnce() {
        if (finishing) return
        finishing = true
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finishOnce()
        return true
    }
}
