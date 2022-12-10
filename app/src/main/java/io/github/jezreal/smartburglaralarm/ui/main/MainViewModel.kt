package io.github.jezreal.smartburglaralarm.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jezreal.smartburglaralarm.repository.TestApiRepository
import io.github.jezreal.smartburglaralarm.wrappers.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: TestApiRepository
) : ViewModel() {

    private val _mainEvent = MutableSharedFlow<MainEvent>()
    val mainEvent = _mainEvent.asSharedFlow()

    private val _mainState = MutableStateFlow<MainState>(MainState.Empty)
    val mainState = _mainState.asLiveData()

    fun showSnackBar(message: String, length: Int) {
        viewModelScope.launch {
            _mainEvent.emit(MainEvent.ShowSnackBar(message, length))
        }
    }

    fun getRandomQuote() {
        viewModelScope.launch(Dispatchers.Default) {
            _mainState.emit(MainState.Loading)

            when (val apiResponse = repository.getApiResponse()) {
                is Resource.Success -> {
                    _mainState.emit(MainState.Success(apiResponse.data!!.content))
                }

                is Resource.Error -> {
                    _mainState.emit(MainState.Error(apiResponse.message!!))
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