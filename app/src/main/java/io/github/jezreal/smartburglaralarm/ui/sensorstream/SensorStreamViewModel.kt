package io.github.jezreal.smartburglaralarm.ui.sensorstream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gustavoavila.websocketclient.WebSocketClient
import io.github.jezreal.smartburglaralarm.repository.NodeRepository
import io.github.jezreal.smartburglaralarm.ui.sensorstream.SensorStreamViewModel.SensorStreamEvent.ShowSnackBar
import io.github.jezreal.smartburglaralarm.ui.sensorstream.SensorStreamViewModel.SensorStreamState.SocketConnected
import io.github.jezreal.smartburglaralarm.ui.sensorstream.SensorStreamViewModel.SensorStreamState.SocketDisconnected
import io.github.jezreal.smartburglaralarm.wrappers.Resource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.URI
import java.net.URISyntaxException
import javax.inject.Inject

@HiltViewModel
class SensorStreamViewModel @Inject constructor(
    private val repository: NodeRepository
) : ViewModel() {

    private val _sensorStreamState = MutableStateFlow<SensorStreamState>(SensorStreamState.Empty)
    val sensorStreamState = _sensorStreamState.asLiveData()

    private val _sensorStreamEvent = MutableSharedFlow<SensorStreamEvent>()
    val sensorStreamEvent = _sensorStreamEvent.asSharedFlow()

    private lateinit var socketUri: URI
    private lateinit var socketClient: WebSocketClient

    private fun connectWebSocket() {
        Timber.d("Connecting to socket")
        viewModelScope.launch {
            _sensorStreamEvent.emit(
                ShowSnackBar(
                    "Connecting to socket",
                    Snackbar.LENGTH_SHORT
                )
            )
        }

        viewModelScope.launch {
            _sensorStreamState.emit(SensorStreamState.Loading)

            try {
                socketUri = URI("ws://192.168.4.1:8080/ws")
            } catch (e: URISyntaxException) {
                viewModelScope.launch {
                    _sensorStreamEvent.emit(
                        ShowSnackBar(
                            "Something went wrong",
                            Snackbar.LENGTH_SHORT
                        )
                    )
                }
            }

            socketClient = object : WebSocketClient(socketUri) {
                override fun onOpen() {
                    Timber.d("Socket opened")
                    viewModelScope.launch {
                        _sensorStreamEvent.emit(
                            ShowSnackBar(
                                "Socket connected",
                                Snackbar.LENGTH_SHORT
                            )
                        )
                    }
                }

                override fun onTextReceived(message: String) {
                    viewModelScope.launch {
                        _sensorStreamEvent.emit(
                            ShowSnackBar(
                                message.trim(),
                                Snackbar.LENGTH_SHORT
                            )
                        )
                    }
                }

                override fun onBinaryReceived(data: ByteArray?) {
//
                }

                override fun onPingReceived(data: ByteArray?) {
//
                }

                override fun onPongReceived(data: ByteArray?) {
//
                }

                override fun onException(e: Exception) {
                    viewModelScope.launch {
                        _sensorStreamEvent.emit(
                            ShowSnackBar(
                                e.message.toString(),
                                Snackbar.LENGTH_SHORT
                            )
                        )
                    }
                }

                override fun onCloseReceived() {
                    viewModelScope.launch {
                        _sensorStreamEvent.emit(
                            ShowSnackBar(
                                "OnCloseReceived",
                                Snackbar.LENGTH_SHORT
                            )
                        )
                    }
                }
            }

            socketClient.setConnectTimeout(10_000)
            socketClient.setReadTimeout(60_000)
            socketClient.enableAutomaticReconnection(5_000)

            socketClient.connect()

            _sensorStreamState.emit(SocketConnected)
        }
    }

    fun getDeviceStatus() {
        _sensorStreamState.value = SensorStreamState.Loading
        viewModelScope.launch {

            when(val response = repository.connectToDevice()) {
                is Resource.Success -> {
                    if(response.data!!.alarmStatus == "Alarm on") {
                        connectWebSocket()
                    } else {
                        _sensorStreamState.value = SensorStreamState.DeviceInactive
                    }
                }

                is Resource.Error -> {
                    _sensorStreamState.value = SensorStreamState.Error(response.message!!)
                }
            }
        }
    }

    fun showSnackBar(message: String, length: Int) {
        viewModelScope.launch {
            _sensorStreamEvent.emit(ShowSnackBar(message, length))
        }
    }

    fun closeSocket() {
        if (this::socketClient.isInitialized) {
            socketClient.close()
        }

        viewModelScope.launch {
            _sensorStreamState.emit(SocketDisconnected)
        }
    }

    sealed class SensorStreamState {
        object Empty : SensorStreamState()
        object Loading : SensorStreamState()
        object SocketConnected : SensorStreamState()
        object SocketDisconnected : SensorStreamState()
        object DeviceInactive : SensorStreamState()
        class Error(val message: String) : SensorStreamState()
    }

    sealed class SensorStreamEvent {
        class ShowSnackBar(val message: String, val length: Int) : SensorStreamEvent()
    }
}