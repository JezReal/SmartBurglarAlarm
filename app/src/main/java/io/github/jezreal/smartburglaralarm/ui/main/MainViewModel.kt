package io.github.jezreal.smartburglaralarm.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jezreal.smartburglaralarm.repository.NodeRepository
import io.github.jezreal.smartburglaralarm.ui.main.MainViewModel.MainEvent.ShowSnackBar
import io.github.jezreal.smartburglaralarm.wrappers.Resource.Error
import io.github.jezreal.smartburglaralarm.wrappers.Resource.Success
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: NodeRepository
) : ViewModel() {

    private val _mainEvent = MutableSharedFlow<MainEvent>()
    val mainEvent = _mainEvent.asSharedFlow()

    private val _mainState = MutableStateFlow<MainState>(MainState.Empty)
    val mainState = _mainState.asLiveData()

    fun showSnackBar(message: String, length: Int) {
        viewModelScope.launch {
            _mainEvent.emit(ShowSnackBar(message, length))
        }
    }

    fun getDeviceStatus() {
        _mainState.value = MainState.Loading
        viewModelScope.launch {

            when(val response = repository.connectToDevice()) {
                is Success -> {
                    _mainState.value = MainState.Success(response.data!!.alarmStatus)
                }

                is Error -> {
                    _mainState.value = MainState.Error(response.message!!)
                }
            }
        }
    }

    fun toggleDeviceStatus() {
        _mainState.value = MainState.Loading

        viewModelScope.launch {
            when(val response = repository.toggleAlarmStatus()) {
                is Success -> {
                    _mainState.value = MainState.Success(response.data!!.alarmStatus)
                }
                is Error -> {
                    _mainState.value = MainState.Error(response.message!!)
                }
            }
        }
    }


    sealed class MainState {
        object Empty : MainState()
        object Loading : MainState()
        class Success(val response: String) : MainState()
        class Error(val message: String) : MainState()
    }

    sealed class MainEvent {
        class ShowSnackBar(val message: String, val length: Int) : MainEvent()
    }
}