package io.github.jezreal.smartburglaralarm.ui.sensorstream

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
import io.github.jezreal.smartburglaralarm.databinding.FragmentSensorStreamBinding
import io.github.jezreal.smartburglaralarm.ui.sensorstream.SensorStreamViewModel.SensorStreamEvent.ShowSnackBar
import io.github.jezreal.smartburglaralarm.ui.sensorstream.SensorStreamViewModel.SensorStreamState.*
import kotlinx.coroutines.launch

class SensorStreamFragment : Fragment() {

    private var _binding: FragmentSensorStreamBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SensorStreamViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSensorStreamBinding.inflate(inflater, container, false)

        setListeners()
        observeEvents()
        observeLiveData()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setListeners() {

    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.sensorStreamEvent.collect { event ->
                        when (event) {
                            is ShowSnackBar -> {
                                Snackbar.make(binding.root, event.message, event.length).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun observeLiveData() {
        viewModel.sensorStreamState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Empty -> {
                    viewModel.connectWebSocket()
                }

                is Loading -> {
                    viewModel.showSnackBar("Connecting to device", Snackbar.LENGTH_SHORT)
                }

                is SocketConnected -> {
//                    do nothing
                }

                is SocketDisconnected -> {
//                  do nothing
                }
            }
        }
    }


}