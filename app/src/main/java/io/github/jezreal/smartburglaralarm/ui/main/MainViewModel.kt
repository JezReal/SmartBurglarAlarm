package io.github.jezreal.smartburglaralarm.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _mainEvent = MutableSharedFlow<MainEvent>()
    val mainEvent = _mainEvent.asSharedFlow()

    fun showSnackBar(message: String, length: Int) {
        viewModelScope.launch {
            _mainEvent.emit(MainEvent.ShowSnackBar(message, length))
        }
    }

    sealed class MainEvent {
        class ShowSnackBar(val message: String, val length: Int) : MainEvent()
    }
}