package com.kiuda.app.presentation.sensor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kiuda.app.R
import com.kiuda.app.databinding.FragmentSensorAlertsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SensorAlertsFragment : Fragment() {

    private var _binding: FragmentSensorAlertsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSensorAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAskSensor1.setOnClickListener {
            val bundle = bundleOf(
                "sensorAlertContext" to "💧 토양 수분 18% (건조 주의 단계) - 방울토마토 1구역"
            )
            findNavController().navigate(R.id.action_sensor_to_ask, bundle)
        }

        binding.btnAskSensor2.setOnClickListener {
            val bundle = bundleOf(
                "sensorAlertContext" to "⚠ 토마토 잎곰팡이병 발생 예보 (주의 단계) - A동 온실"
            )
            findNavController().navigate(R.id.action_sensor_to_ask, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
