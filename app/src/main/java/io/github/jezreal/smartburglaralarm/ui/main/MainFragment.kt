package io.github.jezreal.smartburglaralarm.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import io.github.jezreal.smartburglaralarm.databinding.FragmentMainBinding
import io.github.jezreal.smartburglaralarm.ui.main.MainViewModel.MainEvent.ShowSnackBar
import io.github.jezreal.smartburglaralarm.ui.main.MainViewModel.MainState.*
import kotlinx.coroutines.launch

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
        collectFlows()
        observeLiveData()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setListeners() {
        binding.apply {
//            fromTime.setOnClickListener {
//                buildPicker("Select from time")
//                picker.show(requireActivity().supportFragmentManager, "MainFragment")
//            }
//
//            toTime.setOnClickListener {
//                buildPicker("Select to time")
//                picker.show(requireActivity().supportFragmentManager, "MainFragment")
//            }

            activateButton.setOnClickListener {
                viewModel.showSnackBar("hello roiii", Snackbar.LENGTH_SHORT)
            }

           toggleButton.setOnClickListener {
               viewModel.toggleLed()
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

    private fun observeLiveData() {
        viewModel.mainState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Empty -> {
                    viewModel.showSnackBar("Empty state bruh", Snackbar.LENGTH_SHORT)
                }

                is Loading -> {
                    viewModel.showSnackBar("Loading", Snackbar.LENGTH_SHORT)
                }

                is Success -> {
                    binding.ledStatus.text = "Led status: ${state.response}"
                }

                is Error -> {
                    viewModel.showSnackBar(state.message, Snackbar.LENGTH_SHORT)
                }
            }
        }
    }

    private fun collectFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    collectEvents()
                }
            }
        }
    }

    private suspend fun collectEvents() {
        viewModel.mainEvent.collect { event ->
            when (event) {
                is ShowSnackBar -> {
                    Snackbar.make(binding.root, event.message, event.length).show()
                }
            }
        }
    }
}