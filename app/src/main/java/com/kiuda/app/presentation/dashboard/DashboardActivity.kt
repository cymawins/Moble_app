package com.kiuda.app.presentation.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.kiuda.app.R
import com.kiuda.app.databinding.ActivityDashboardBinding
import com.kiuda.app.databinding.ItemPlantBinding
import com.kiuda.app.domain.model.DashboardData
import com.kiuda.app.presentation.ask.AskActivity
import com.kiuda.app.presentation.auth.login.LoginActivity
import com.kiuda.app.presentation.ncpms.EncyclopediaListActivity
import com.kiuda.app.presentation.ncpms.PestAlertsActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.tvGreeting.text = "오늘도 건강하게 자라고 있어요, ${viewModel.greetingName}님"

        val onAddPlantClick = View.OnClickListener {
            Toast.makeText(this, "🌱 새로운 키움이 등록 기능 준비 중입니다.", Toast.LENGTH_SHORT).show()
        }
        binding.btnAddPlant.setOnClickListener(onAddPlantClick)
        binding.btnRegisterFirstPlant.setOnClickListener(onAddPlantClick)

        binding.btnAsk.setOnClickListener {
            startActivity(Intent(this, AskActivity::class.java))
        }
        binding.btnPestAlerts.setOnClickListener {
            startActivity(Intent(this, PestAlertsActivity::class.java))
        }
        binding.btnEncyclopedia.setOnClickListener {
            startActivity(Intent(this, EncyclopediaListActivity::class.java))
        }

        binding.swipeRefresh.setColorSchemeColors(
            ContextCompat.getColor(this, R.color.kiuda_primary)
        )
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.load()
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.swipeRefresh.isRefreshing = state is DashboardUiState.Loading
                when (state) {
                    is DashboardUiState.Loading -> {
                        binding.progress.visibility = View.VISIBLE
                    }
                    is DashboardUiState.Success -> {
                        binding.progress.visibility = View.GONE
                        val d = state.data
                        if (d.plants.isEmpty() && d.checklist.isEmpty() && d.weather == null && d.pestAlerts.isEmpty()) {
                            renderDemoData()
                        } else {
                            renderDashboard(d)
                        }
                    }
                    is DashboardUiState.Error -> {
                        binding.progress.visibility = View.GONE
                        Toast.makeText(
                            this@DashboardActivity,
                            "서버 연결 없음 · 데모 화면 표시",
                            Toast.LENGTH_SHORT
                        ).show()
                        renderDemoData()
                    }
                }
            }
        }

        viewModel.load()
    }

    private fun renderDashboard(data: DashboardData) {
        val textColor = ContextCompat.getColor(this, R.color.kiuda_text_primary)

        binding.containerPlants.removeAllViews()
        if (data.plants.isEmpty()) {
            binding.tvNoPlants.visibility = View.VISIBLE
        } else {
            binding.tvNoPlants.visibility = View.GONE
            data.plants.forEach { plant ->
                val itemBinding = ItemPlantBinding.inflate(layoutInflater, binding.containerPlants, false)
                itemBinding.tvPlantNickname.text = plant.nickname ?: plant.name ?: "키움이"
                itemBinding.tvPlantInfo.text = "${plant.plantType ?: "작물"}  ·  ${plant.location ?: "베란다 가든"}"
                itemBinding.tvPlantStatusTag.text = "생육중"
                binding.containerPlants.addView(itemBinding.root)
            }
        }

        binding.containerChecklist.removeAllViews()
        data.checklist.forEach { item ->
            val cb = CheckBox(this).apply {
                text = item.title ?: ""
                isChecked = item.completed
                setTextColor(textColor)
                textSize = 15f
                setPadding(8, 4, 8, 4)
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked && item.id != null) {
                        viewModel.completeChecklist(item.id)
                    }
                }
            }
            binding.containerChecklist.addView(cb)
        }

        data.weather?.let { w ->
            binding.tvWeather.text = "${w.temperature?.toInt() ?: 28}°C / ${w.condition ?: "맑음"}"
        } ?: run {
            binding.tvWeather.text = "28°C / 맑음"
        }

        binding.containerPest.removeAllViews()
        val alertColor = ContextCompat.getColor(this, R.color.kiuda_text_primary)
        data.pestAlerts.forEach { alert ->
            val tv = TextView(this).apply {
                text = "⚠  ${alert.title ?: "주의보"} (${alert.level ?: "MEDIUM"})"
                textSize = 14f
                setTextColor(alertColor)
                setPadding(8, 8, 8, 8)
            }
            binding.containerPest.addView(tv)
        }
    }

    private fun renderDemoData() {
        val textColor = ContextCompat.getColor(this, R.color.kiuda_text_primary)
        val alertColor = ContextCompat.getColor(this, R.color.kiuda_warning)

        binding.tvNoPlants.visibility = View.GONE
        binding.containerPlants.removeAllViews()

        val demoPlants = listOf(
            Pair("방울토마토", "토마토  ·  베란다 가든"),
            Pair("청양고추", "고추  ·  옥상 정원")
        )
        demoPlants.forEach { (nickname, info) ->
            val itemBinding = ItemPlantBinding.inflate(layoutInflater, binding.containerPlants, false)
            itemBinding.tvPlantNickname.text = nickname
            itemBinding.tvPlantInfo.text = info
            itemBinding.tvPlantStatusTag.text = "생육중"
            binding.containerPlants.addView(itemBinding.root)
        }

        binding.containerChecklist.removeAllViews()
        listOf("오늘 물 주기", "잎 상태 확인", "비료 주기").forEachIndexed { i, title ->
            val cb = CheckBox(this).apply {
                text = title
                isChecked = i == 0
                setTextColor(textColor)
                textSize = 15f
                setPadding(8, 4, 8, 4)
            }
            binding.containerChecklist.addView(cb)
        }

        binding.tvWeather.text = "28°C / 맑음"

        binding.containerPest.removeAllViews()
        val tv = TextView(this).apply {
            text = "⚠  병해충 주의보 (MEDIUM) - 탄저병 주의"
            textSize = 14f
            setTextColor(alertColor)
            setPadding(8, 8, 8, 8)
        }
        binding.containerPest.addView(tv)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_dashboard, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                viewModel.logout()
                startActivity(Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
