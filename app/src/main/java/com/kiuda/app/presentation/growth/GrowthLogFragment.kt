package com.kiuda.app.presentation.growth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.kiuda.app.R
import com.kiuda.app.databinding.FragmentGrowthLogBinding
import com.kiuda.app.domain.model.GrowthLogUiState
import com.kiuda.app.domain.model.UserPlant
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GrowthLogFragment : Fragment() {

    private var _binding: FragmentGrowthLogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GrowthLogViewModel by viewModels()

    private val adapter = GrowthRecordAdapter { item ->
        Toast.makeText(requireContext(), "${item.plantName}: ${item.title}", Toast.LENGTH_SHORT).show()
    }

    private var isChipGroupInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGrowthLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvGrowthLog.layoutManager = LinearLayoutManager(requireContext())
        binding.rvGrowthLog.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadData()
        }
        binding.btnEmptyAction.setOnClickListener {
            viewModel.selectPlantFilter(null)
            viewModel.loadData()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is GrowthLogUiState.Loading -> {
                            binding.progress.visibility = View.VISIBLE
                            binding.swipeRefresh.isRefreshing = false
                        }
                        is GrowthLogUiState.Success -> {
                            binding.progress.visibility = View.GONE
                            binding.swipeRefresh.isRefreshing = false

                            // 1. 작물 필터 칩 설정 (1회 초기화 및 선택 상태 보장)
                            setupCropFilterChips(state.plants, state.selectedPlantId)

                            // 2. 상단 통계 수치 업데이트
                            binding.tvStatWatering.text = "${state.stats.weeklyWateringCount}회"
                            binding.tvStatPlants.text = "${state.stats.totalActivePlants}개"
                            binding.tvStatStreak.text = "${state.stats.streakDays}일"

                            // 3. 기록 리스트 및 빈 상태 처리
                            if (state.records.isEmpty()) {
                                binding.rvGrowthLog.visibility = View.GONE
                                binding.layoutEmptyState.visibility = View.VISIBLE
                            } else {
                                binding.rvGrowthLog.visibility = View.VISIBLE
                                binding.layoutEmptyState.visibility = View.GONE
                                adapter.submitList(state.records)
                            }
                        }
                        is GrowthLogUiState.Error -> {
                            binding.progress.visibility = View.GONE
                            binding.swipeRefresh.isRefreshing = false
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun setupCropFilterChips(plants: List<UserPlant>, selectedPlantId: Long?) {
        if (isChipGroupInitialized) return
        isChipGroupInitialized = true

        binding.chipGroupCrops.removeAllViews()

        // "전체" 칩
        val allChip = Chip(requireContext()).apply {
            text = "전체"
            isCheckable = true
            isChecked = (selectedPlantId == null)
            setOnClickListener {
                viewModel.selectPlantFilter(null)
            }
        }
        binding.chipGroupCrops.addView(allChip)

        // 개별 작물 칩
        plants.forEach { plant ->
            val displayName = plant.nickname ?: plant.name ?: "작물"
            val plantChip = Chip(requireContext()).apply {
                text = "$displayName (${plant.plantType ?: "관엽"})"
                isCheckable = true
                isChecked = (selectedPlantId == plant.id)
                setOnClickListener {
                    viewModel.selectPlantFilter(plant.id)
                }
            }
            binding.chipGroupCrops.addView(plantChip)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        isChipGroupInitialized = false
    }
}
