package io.github.jezreal.smartburglaralarm.ui.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    fun logSomething() {
        Timber.d("Heyyyy")
    }
}