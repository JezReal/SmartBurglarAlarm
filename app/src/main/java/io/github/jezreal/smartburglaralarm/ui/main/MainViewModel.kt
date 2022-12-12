package io.github.jezreal.smartburglaralarm.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gustavoavila.websocketclient.WebSocketClient
import io.github.jezreal.smartburglaralarm.repository.TestApiRepository
import io.github.jezreal.smartburglaralarm.ui.main.MainViewModel.MainEvent.ShowSnackBar
import io.github.jezreal.smartburglaralarm.wrappers.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.URI
import java.net.URISyntaxException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: TestApiRepository
) : ViewModel() {

    private val _mainEvent = MutableSharedFlow<MainEvent>()
    val mainEvent = _mainEvent.asSharedFlow()

    private val _mainState = MutableStateFlow<MainState>(MainState.Empty)
    val mainState = _mainState.asLiveData()

    private lateinit var socketUri: URI
    private lateinit var socketClient: WebSocketClient

    fun showSnackBar(message: String, length: Int) {
        viewModelScope.launch {
            _mainEvent.emit(ShowSnackBar(message, length))
        }
    }

    fun toggleLed() {
        viewModelScope.launch(Dispatchers.Default) {
            _mainState.emit(MainState.Loading)

            when (val apiResponse = repository.toggleLed()) {
                is Resource.Success -> {
                    _mainState.emit(MainState.Success(apiResponse.data!!.status))
                }

                is Resource.Error -> {
                    _mainState.emit(MainState.Error(apiResponse.message!!))
                }
            }
        }
    }

    fun connectWebSocket() {
        Timber.d("Connecting to socket")
        viewModelScope.launch {
            _mainEvent.emit(ShowSnackBar("Connecting to socket", Snackbar.LENGTH_SHORT))
        }

        viewModelScope.launch {
            _mainState.emit(MainState.SocketLoading)

            try {
                socketUri = URI("ws://192.168.4.1:8080/ws")
            } catch (e: URISyntaxException) {
                viewModelScope.launch {
                    _mainEvent.emit(ShowSnackBar("Something went wrong", Snackbar.LENGTH_SHORT))
                }
            }

            socketClient = object : WebSocketClient(socketUri) {
                override fun onOpen() {
                    Timber.d("Socket opened")
                    viewModelScope.launch {
                        _mainEvent.emit(ShowSnackBar("Socket connected", Snackbar.LENGTH_SHORT))
                    }
                }

                override fun onTextReceived(message: String) {
                    viewModelScope.launch {
                        _mainEvent.emit(ShowSnackBar(message, Snackbar.LENGTH_SHORT))
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
                        _mainEvent.emit(ShowSnackBar(e.message.toString(), Snackbar.LENGTH_SHORT))
                    }
                }

                override fun onCloseReceived() {
                    viewModelScope.launch {
                        _mainEvent.emit(ShowSnackBar("OnCloseReceived", Snackbar.LENGTH_SHORT))
                    }
                }
            }

            socketClient.setConnectTimeout(10_000)
            socketClient.setReadTimeout(60_000)
            socketClient.enableAutomaticReconnection(5_000)

            Timber.d("Connected?")
            socketClient.connect()

            _mainState.emit(MainState.SocketConnected)
        }

    }

    fun closeSocket() {
        socketClient.close()
        viewModelScope.launch {
            _mainState.emit(MainState.SocketDisconnected)
        }
    }

    sealed class MainState {
        object Empty : MainState()
        object Loading : MainState()
        class Success(val response: String) : MainState()
        class Error(val message: String) : MainState()
        object SocketConnected : MainState()
        object SocketDisconnected : MainState()
        object SocketLoading : MainState()
    }

    sealed class MainEvent {
        class ShowSnackBar(val message: String, val length: Int) : MainEvent()
    }
}