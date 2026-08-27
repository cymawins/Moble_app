package com.kiuda.app.presentation.ask

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.kiuda.app.R
import com.kiuda.app.databinding.FragmentAskBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AskFragment : Fragment() {

    private var _binding: FragmentAskBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AskViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contextArg = arguments?.getString("sensorAlertContext")
        if (!contextArg.isNullOrEmpty()) {
            binding.cardSensorContext.visibility = View.VISIBLE
            binding.tvSensorContextText.text = "연결된 센서 컨텍스트: $contextArg"
            binding.etInput.setText("$contextArg 에 대해 해결 방안을 알려줘.")
        } else {
            binding.cardSensorContext.visibility = View.GONE
        }

        binding.btnSend.setOnClickListener {
            val text = binding.etInput.text.toString().trim()
            if (text.isNotEmpty()) {
                addMessage("나: $text", true)
                binding.etInput.text?.clear()
                viewModel.diagnose(text)
            }
        }

        binding.btnCamera.setOnClickListener {
            startActivity(Intent(requireContext(), AskActivity::class.java))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is AskUiState.Diagnosing, is AskUiState.Uploading, is AskUiState.AnalyzingPhoto, is AskUiState.Predicting -> {
                        // 진단 및 분석 수행 중
                    }
                    is AskUiState.Success -> {
                        val diag = state.result
                        val mgmt = diag.managementMethods?.joinToString("\n• ", "관리법:\n• ") ?: ""
                        addMessage("키:우다 AI: [진단: ${diag.diagnosisName ?: "상태 확인"}]\n${diag.reason ?: ""}\n$mgmt", false)
                    }
                    is AskUiState.Error -> {
                        addMessage("키:우다 AI: ${state.message}", false)
                    }
                    else -> {}
                }
            }
        }

        addMessage("키:우다 AI 농업 비서입니다. 작물 상태나 진단하고 싶은 증상을 물어보세요! 🌿", false)
    }

    private fun addMessage(message: String, isUser: Boolean) {
        val tv = TextView(requireContext()).apply {
            text = message
            textSize = 15f
            setPadding(20, 16, 20, 16)
            setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (isUser) R.color.kiuda_primary else R.color.kiuda_text_primary
                )
            )
        }
        binding.containerMessages.addView(tv)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
