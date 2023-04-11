package com.boredream.koalatrace.common.vmcompose

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.boredream.koalatrace.data.ResponseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 请求类 view model 组件
 * 不影响架构，用于节省模版代码
 * VM + View ... 应该是配套的一组
 */
class RequestVMCompose<T>(
    private val viewModelScope: CoroutineScope,
) {
    private var fetchJob: Job? = null

    private val _isRequestingUiState = MutableLiveData<Boolean>()
    val isRequestingUiState: LiveData<Boolean> = _isRequestingUiState

    private val _failUiState = MutableLiveData<ResponseEntity<T>>()
    val failUiState: LiveData<ResponseEntity<T>> = _failUiState

    private val _successUiState = MutableLiveData<ResponseEntity<T>>()
    val successUiState: LiveData<ResponseEntity<T>> = _successUiState

    fun request(onSuccess: (data: T) -> Unit = {}, repoRequest: suspend () -> ResponseEntity<T>) {
        _isRequestingUiState.value = true

        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                val response = repoRequest.invoke()
                if (response.isSuccess()) {
                    _successUiState.value = response
                    onSuccess.invoke(response.getSuccessData())
                } else {
                    _failUiState.value = response
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _failUiState.value = ResponseEntity.httpError(e)
            } finally {
                _isRequestingUiState.value = false
            }
        }
    }

}