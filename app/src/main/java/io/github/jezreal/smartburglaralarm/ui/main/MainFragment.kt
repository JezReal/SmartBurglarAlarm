package io.github.jezreal.smartburglaralarm.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import io.github.jezreal.smartburglaralarm.databinding.FragmentMainBinding

@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var picker: MaterialTimePicker
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        setListeners()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setListeners() {
        binding.apply {
            fromTime.setOnClickListener {
                buildPicker("Select from time")
                picker.show(requireActivity().supportFragmentManager, "MainFragment")
            }

            toTime.setOnClickListener {
                buildPicker("Select to time")
                picker.show(requireActivity().supportFragmentManager, "MainFragment")
            }

            button.setOnClickListener {
                viewModel.logSomething()
            }
        }
    }

    private fun buildPicker(title: String) {
        picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(10)
            .setTitleText(title)
            .build()
    }
}