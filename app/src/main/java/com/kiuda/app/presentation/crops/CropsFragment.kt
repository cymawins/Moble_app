package com.kiuda.app.presentation.crops

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.kiuda.app.R
import com.kiuda.app.databinding.FragmentCropsBinding
import com.kiuda.app.domain.model.DashboardData
import com.kiuda.app.presentation.dashboard.DashboardUiState
import com.kiuda.app.presentation.dashboard.DashboardViewModel
import com.kiuda.app.presentation.ncpms.EncyclopediaListActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CropsFragment : Fragment() {

    private var _binding: FragmentCropsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCropsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvGreeting.text = "안녕하세요, ${viewModel.greetingName}님"

        binding.btnEncyclopedia.setOnClickListener {
            startActivity(Intent(requireContext(), EncyclopediaListActivity::class.java))
        }

        binding.swipeRefresh.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.kiuda_primary)
        )
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.load()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.swipeRefresh.isRefreshing = state is DashboardUiState.Loading
                when (state) {
                    is DashboardUiState.Loading -> {
                        binding.progress.visibility = View.VISIBLE
                    }
                    is DashboardUiState.Success -> {
                        binding.progress.visibility = View.GONE
                        val d = state.data
                        if (d.plants.isEmpty() && d.checklist.isEmpty() && d.weather == null) {
                            renderDemoData()
                        } else {
                            renderCrops(d)
                        }
                    }
                    is DashboardUiState.Error -> {
                        binding.progress.visibility = View.GONE
                        Toast.makeText(requireContext(), "서버 연결 데이터 표시", Toast.LENGTH_SHORT).show()
                        renderDemoData()
                    }
                }
            }
        }

        viewModel.load()
    }

    private fun renderCrops(data: DashboardData) {
        val textColor = ContextCompat.getColor(requireContext(), R.color.kiuda_text_primary)
        binding.containerPlants.removeAllViews()

        if (data.plants.isEmpty()) {
            binding.tvNoPlants.visibility = View.VISIBLE
        } else {
            binding.tvNoPlants.visibility = View.GONE
            data.plants.forEach { plant ->
                val tv = TextView(requireContext()).apply {
                    text = "🌱  ${plant.nickname ?: plant.name ?: "작물"}"
                    textSize = 16f
                    setTextColor(textColor)
                    setPadding(24, 16, 24, 16)
                }
                binding.containerPlants.addView(tv)
            }
        }

        data.weather?.let { w ->
            binding.tvWeather.text = "☀  ${w.temperature?.toInt() ?: "-"}°C / ${w.condition ?: "정보 없음"}"
        }
    }

    private fun renderDemoData() {
        val textColor = ContextCompat.getColor(requireContext(), R.color.kiuda_text_primary)
        binding.tvNoPlants.visibility = View.GONE
        binding.containerPlants.removeAllViews()

        listOf("방울토마토 (A동 온실)", "청양고추 (노지 2구역)", "상추 (수경재배)").forEach {
            val tv = TextView(requireContext()).apply {
                text = "🌱  $it"
                textSize = 16f
                setTextColor(textColor)
                setPadding(24, 16, 24, 16)
            }
            binding.containerPlants.addView(tv)
        }

        binding.tvWeather.text = "☀  28°C / 맑음"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
