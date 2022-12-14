package io.github.jezreal.smartburglaralarm.ui.sensorstream

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.github.jezreal.smartburglaralarm.R
import io.github.jezreal.smartburglaralarm.databinding.FragmentSensorStreamBinding
import io.github.jezreal.smartburglaralarm.ui.sensorstream.SensorStreamViewModel.SensorStreamEvent.BurglarDetected
import io.github.jezreal.smartburglaralarm.ui.sensorstream.SensorStreamViewModel.SensorStreamEvent.ShowSnackBar
import io.github.jezreal.smartburglaralarm.ui.sensorstream.SensorStreamViewModel.SensorStreamState.*
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SensorStreamFragment : Fragment() {

    private var _binding: FragmentSensorStreamBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SensorStreamViewModel by activityViewModels()
    private lateinit var adapter: SensorDataAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSensorStreamBinding.inflate(inflater, container, false)

        setRecyclerViewAdapter()
        setListeners()
        observeEvents()
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
                            is BurglarDetected -> {
//                                do nothing. Let the activity listen for it
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
                    binding.recyclerView.visibility = View.GONE
                    binding.errorMessage.visibility = View.GONE
                }

                is Loading -> {
                    viewModel.showSnackBar("Connecting to device", Snackbar.LENGTH_SHORT)
                }

                is SocketConnected -> {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.errorMessage.visibility = View.GONE
                }

                is SocketDisconnected -> {
//                  do nothing
                }

                is Error -> {
                    viewModel.showSnackBar(state.message, Snackbar.LENGTH_SHORT)
                    binding.recyclerView.visibility = View.GONE
                }

                is DeviceInactive -> {
                    binding.errorMessage.text = resources.getString(R.string.device_inactive)
                    binding.errorMessage.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                }
            }
        }

        viewModel.sensorDataStream.observe(viewLifecycleOwner) { sensorData ->
            binding.recyclerView.visibility = View.VISIBLE
            adapter.submitList(sensorData)
            binding.recyclerView.smoothScrollToPosition(adapter.itemCount)
        }
    }

    private fun setRecyclerViewAdapter() {
        adapter = SensorDataAdapter()
        binding.recyclerView.adapter = adapter
    }
}