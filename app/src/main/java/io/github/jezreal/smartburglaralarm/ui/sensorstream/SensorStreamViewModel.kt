package io.github.jezreal.smartburglaralarm.ui.sensorstream

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gustavoavila.websocketclient.WebSocketClient
import io.github.jezreal.smartburglaralarm.domain.SensorData
import io.github.jezreal.smartburglaralarm.repository.NodeRepository
import io.github.jezreal.smartburglaralarm.ui.sensorstream.SensorStreamViewModel.SensorStreamEvent.BurglarDetected
import io.github.jezreal.smartburglaralarm.ui.sensorstream.SensorStreamViewModel.SensorStreamEvent.ShowSnackBar
import io.github.jezreal.smartburglaralarm.ui.sensorstream.SensorStreamViewModel.SensorStreamState.*
import io.github.jezreal.smartburglaralarm.wrappers.Resource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import java.net.URI
import java.net.URISyntaxException
import javax.inject.Inject

@HiltViewModel
class SensorStreamViewModel @Inject constructor(
    private val repository: NodeRepository
) : ViewModel() {

    private val _sensorStreamState = MutableStateFlow<SensorStreamState>(Empty)
    val sensorStreamState = _sensorStreamState.asLiveData()

    private val _sensorStreamEvent = MutableSharedFlow<SensorStreamEvent>()
    val sensorStreamEvent = _sensorStreamEvent.asSharedFlow()

    private val _sensorDataStream = MutableStateFlow(emptyList<SensorData>())
    val sensorDataStream: LiveData<List<SensorData>> = _sensorDataStream.asLiveData()

    private var sensorDataId = 1L

    private lateinit var socketUri: URI
    private lateinit var socketClient: WebSocketClient

    fun connectWebSocket() {
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
            _sensorStreamState.value = Loading

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
                    val sensorData = addSensorData(message)
                    _sensorDataStream.value = _sensorDataStream.value.toMutableList() + sensorData
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

            _sensorStreamState.value = SocketConnected
        }
    }

    fun getDeviceStatus() {
        _sensorStreamState.value = Loading
        viewModelScope.launch {

            when (val response = repository.connectToDevice()) {
                is Resource.Success -> {
                    if (response.data!!.alarmStatus == "Alarm off") {
                        _sensorStreamState.value = DeviceInactive
                    } else {
                        _sensorStreamState.value = SocketConnected
                    }
                }

                is Resource.Error -> {
                    _sensorStreamState.value = Error(response.message!!)
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
            _sensorStreamState.value = SocketDisconnected
        }
    }

    private fun addSensorData(message: String): SensorData {
        val timeStamp = DateTime.now().toLocalTime()
        val formattedTimeStamp = DateTimeFormat.forPattern("hh:mm:ss aa")

        sensorDataId++

        if (message.trim() == "1") {
            viewModelScope.launch {
                _sensorStreamEvent.emit(BurglarDetected(sensorDataId))
            }
        }

        val parsedMessage = if (message.trim() == "0") {
            "No motion"
        } else {
            "Motion detected"
        }

        return SensorData(
            sensorDataId++,
            parsedMessage,
            formattedTimeStamp.print(timeStamp)
        )
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
        class BurglarDetected(val id: Long) : SensorStreamEvent()
    }
}