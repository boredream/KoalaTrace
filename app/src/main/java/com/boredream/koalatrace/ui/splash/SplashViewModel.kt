package com.boredream.koalatrace.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.boredream.koalatrace.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() : BaseViewModel() {

    private val _uiState = MutableLiveData<String>()
    val uiState: LiveData<String> = _uiState

    fun autoLogin() {
        _uiState.value = uiState.value
    }
}
