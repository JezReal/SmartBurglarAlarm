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

    override fun onResume() {
        super.onResume()
        viewModel.getDeviceStatus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setListeners() {
        binding.apply {
            activateToggleButton.isEnabled = false
            activateToggleButton.text = resources.getString(R.string.alarm_loading)
            activateToggleButton.setOnClickListener {
                activateToggleButton.text = resources.getString(R.string.alarm_status)
                viewModel.showSnackBar("Toggling alarm...", Snackbar.LENGTH_SHORT)
            }

            activateToggleButton.setOnClickListener {
                viewModel.toggleDeviceStatus()
            }

            connectToNodeButton.setOnClickListener {
                viewModel.getDeviceStatus()
            }

        }
    }

    private fun observeLiveData() {
        viewModel.mainState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Empty -> {
                    viewModel.getDeviceStatus()
                }

                is Loading -> {
                    binding.apply {
                        activateToggleButton.isEnabled = false
                        activateToggleButton.text = resources.getString(R.string.alarm_loading)
                        activateToggleButton.setBackgroundResource(R.drawable.activate_button_background_sleeping)
                    }
                }

                is Success -> {
                    if (state.response == "Alarm on") {
                        setAlarmRunning()
                    } else {
                        setAlarmSleeping()
                    }
                }

                is Error -> {
                    binding.activateToggleButton.text = resources.getString(R.string.alarm_error)
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

    private fun setAlarmRunning() {
        binding.apply {
            activateToggleButton.isEnabled = true
            activateToggleButton.text = resources.getString(R.string.alarm_10_is_10_running)
            activateToggleButton.typeface = Typeface.create("sans-serif-medium", Typeface.ITALIC)
            activateToggleButton.setBackgroundResource(R.drawable.activate_button_background_running)
        }
    }

    private fun setAlarmSleeping() {
        binding.apply {
            activateToggleButton.isEnabled = true
            activateToggleButton.text = resources.getString(R.string.alarm_10_is_10_sleeping)
            activateToggleButton.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            activateToggleButton.setBackgroundResource(R.drawable.activate_button_background_sleeping)
        }
    }
}