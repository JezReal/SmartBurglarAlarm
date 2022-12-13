package io.github.jezreal.smartburglaralarm.ui.main

import android.graphics.Typeface
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
import io.github.jezreal.smartburglaralarm.R
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

            activateToggleButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel.showSnackBar("hello roiii", Snackbar.LENGTH_SHORT)
                    activateToggleButton.setTypeface(Typeface.create("sans-serif-medium", Typeface.ITALIC))
                    activateToggleButton.setBackgroundResource(R.drawable.activate_button_background_running)
                } else {
                    viewModel.showSnackBar("hi jezzz", Snackbar.LENGTH_SHORT)
                    activateToggleButton.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL))
                    activateToggleButton.setBackgroundResource(R.drawable.activate_button_background_sleeping)
                }
            }

            toggleButton.setOnClickListener {
               viewModel.toggleLed()
            }

            websocketButton.setOnClickListener {
                if (websocketButton.text == "Connect") {
                    viewModel.connectWebSocket()
                } else {
                    viewModel.closeSocket()
                }
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
//                do nothing
                }

                is Loading -> {
//                    do nothing
                }

                is Success -> {
                    binding.ledStatus.text = "Led status: ${state.response}"
                }

                is Error -> {
                    viewModel.showSnackBar(state.message, Snackbar.LENGTH_SHORT)
                }

                is SocketConnected -> {
                    binding.websocketButton.text = "Disconnect"
                }

                is SocketDisconnected -> {
                    binding.websocketButton.text = "Connect"
                }

                is SocketLoading -> {
                    viewModel.showSnackBar("Connecting", Snackbar.LENGTH_SHORT)
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

    override fun onDestroy() {
        super.onDestroy()
        viewModel.closeSocket()
    }
}