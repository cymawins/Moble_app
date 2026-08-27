package com.kiuda.app.presentation.ncpms

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kiuda.app.R
import com.kiuda.app.databinding.ActivityPestAlertsBinding
import com.kiuda.app.databinding.ItemAlertBinding
import com.kiuda.app.domain.model.AlertLevel
import com.kiuda.app.domain.model.SensorPestAlertItem
import com.kiuda.app.domain.repository.NcpmsRepository
import com.kiuda.app.presentation.ask.AskActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PestAlertsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPestAlertsBinding

    @Inject
    lateinit var repo: NcpmsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPestAlertsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.kiuda_text_primary))
        binding.swipe.setColorSchemeColors(ContextCompat.getColor(this, R.color.kiuda_primary))

        binding.swipe.setOnRefreshListener { loadAlerts() }
        loadAlerts()
    }

    private fun loadAlerts() {
        lifecycleScope.launch {
            binding.swipe.isRefreshing = true
            val repoResult = repo.getAlerts()
            binding.swipe.isRefreshing = false

            // 로컬 1차 센서 & 병해충 통합 목업 데이터 생성
            val alerts = getMockSensorPestAlerts()
            renderAlerts(alerts)
        }
    }

    private fun renderAlerts(items: List<SensorPestAlertItem>) {
        binding.container.removeAllViews()

        if (items.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.tvBadgeHighCount.visibility = View.GONE
            binding.tvBadgeMedCount.visibility = View.GONE
            binding.tvBadgeLowCount.visibility = View.GONE
            return
        }

        binding.layoutEmptyState.visibility = View.GONE

        // 카운터 브레이크다운 계산
        val highCount = items.count { it.level == AlertLevel.HIGH }
        val medCount = items.count { it.level == AlertLevel.MEDIUM }
        val lowCount = items.count { it.level == AlertLevel.LOW }

        binding.tvBadgeHighCount.text = "위험 ${highCount}건"
        binding.tvBadgeMedCount.text = "경고 ${medCount}건"
        binding.tvBadgeLowCount.text = "정보 ${lowCount}건"

        items.forEach { alert ->
            val itemBinding = ItemAlertBinding.inflate(layoutInflater, binding.container, false)

            itemBinding.tvPlantName.text = "🌱 ${alert.plantName}  ·  ${alert.location ?: "일반 구역"}"
            itemBinding.tvAlertTitle.text = alert.title
            itemBinding.tvMetrics.text = alert.metricsText
            itemBinding.tvDescription.text = alert.description

            // 위험 수준별 색상 및 태그 차별화 (Design Token)
            when (alert.level) {
                AlertLevel.HIGH -> {
                    itemBinding.tvAlertBadge.text = "위험 / 경보"
                    itemBinding.tvAlertBadge.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.kiuda_error_container)
                    itemBinding.tvAlertBadge.setTextColor(
                        ContextCompat.getColor(this, R.color.kiuda_error)
                    )
                }
                AlertLevel.MEDIUM -> {
                    itemBinding.tvAlertBadge.text = "경고 / 주의보"
                    itemBinding.tvAlertBadge.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.kiuda_warning_container)
                    itemBinding.tvAlertBadge.setTextColor(
                        ContextCompat.getColor(this, R.color.kiuda_warning)
                    )
                }
                AlertLevel.LOW -> {
                    itemBinding.tvAlertBadge.text = "주의 / 정보"
                    itemBinding.tvAlertBadge.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.kiuda_info_container)
                    itemBinding.tvAlertBadge.setTextColor(
                        ContextCompat.getColor(this, R.color.kiuda_info)
                    )
                }
            }

            // [왜 그런가요?] 버튼 -> 설명 다이얼로그 팝업
            itemBinding.btnWhyGuide.setOnClickListener {
                showWhyGuideDialog(alert)
            }

            // [AI 묻다] 버튼 -> AskActivity로 알림 데이터 파이프라인 전달
            itemBinding.btnAskAi.setOnClickListener {
                navigateToAskActivity(alert)
            }

            binding.container.addView(itemBinding.root)
        }
    }

    private fun showWhyGuideDialog(alert: SensorPestAlertItem) {
        MaterialAlertDialogBuilder(this)
            .setTitle("💡 ${alert.title}")
            .setMessage("【원인 및 가이드】\n${alert.causeAndGuide}")
            .setPositiveButton("확인", null)
            .setNeutralButton("AI에게 상담받기") { _, _ ->
                navigateToAskActivity(alert)
            }
            .show()
    }

    private fun navigateToAskActivity(alert: SensorPestAlertItem) {
        val intent = Intent(this, AskActivity::class.java).apply {
            putExtra(AskActivity.EXTRA_PREFILLED_QUESTION, alert.prefilledQuestion)
            putExtra(AskActivity.EXTRA_PLANT_NAME, alert.plantName)
            putExtra(AskActivity.EXTRA_ALERT_TITLE, alert.title)
            putExtra(AskActivity.EXTRA_ALERT_LEVEL, alert.level.name)
        }
        startActivity(intent)
    }

    private fun getMockSensorPestAlerts(): List<SensorPestAlertItem> {
        return listOf(
            SensorPestAlertItem(
                id = "alert_01",
                plantName = "방울토마토",
                location = "베란다 가든 1구역",
                title = "💧 토양 수분 부족 경고 (15%)",
                level = AlertLevel.HIGH,
                metricsText = "측정 수치: 토양수분 15% (적정 35% 미만) · 온도 29°C",
                description = "최근 24시간 동안 토양 수분이 급격히 감소했습니다. 빠른 관수가 필요합니다.",
                causeAndGuide = "토양 수분이 20% 이하로 지속되면 뿌리의 지지력 감소 및 시듦 현상이 시작됩니다. 25°C 전후의 물을 약 500ml 나누어 천천히 공급해 주세요.",
                prefilledQuestion = "방울토마토 토양 수분이 15%로 급감했습니다. 수분 관리와 응급 조치 방법을 알려주세요."
            ),
            SensorPestAlertItem(
                id = "alert_02",
                plantName = "청양고추",
                location = "옥상 정원 A동",
                title = "⚠ 잎곰팡이병 발생 주의보",
                level = AlertLevel.MEDIUM,
                metricsText = "측정 수치: 상대습도 82% (고습 상태) · 평균 26°C",
                description = "주변 상대습도가 높아 잎곰팡이병 곰팡이 포자 활동 가능성이 높습니다.",
                causeAndGuide = "습도 80% 이상 지속 시 잎 뒤면에 포자가 형성되기 쉽습니다. 통풍 팬을 가동하거나 통풍을 강화하고 물을 줄 때 잎에 닿지 않도록 주의하세요.",
                prefilledQuestion = "청양고추에 잎곰팡이병 주의보가 발령되었습니다. 초기 예방법과 방제 팁을 알려주세요."
            ),
            SensorPestAlertItem(
                id = "alert_03",
                plantName = "루콜라",
                location = "실내 수경재배기",
                title = "💡 영양액 교체 주기 안내",
                level = AlertLevel.LOW,
                metricsText = "측정 수치: EC 1.2 / pH 6.2 (정상 범주)",
                description = "영양액 투여 후 14일이 경과했습니다. 양분 불균형 방지를 위해 교체를 권장합니다.",
                causeAndGuide = "수경재배 2주 경과 시 미량 요소의 불균형이 발생할 수 있습니다. 수조를 세척하고 맑은 수액으로 새 영양액을 조제해 주세요.",
                prefilledQuestion = "루콜라 수경재배 영양액 교체 방법과 최적의 EC/pH 관리법이 궁금해요."
            )
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
