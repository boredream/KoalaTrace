package com.boredream.koalatrace.common

sealed class SimpleRequestUiState<T>
data class SimpleRequestSuccess<T>(val data: T) : SimpleRequestUiState<T>()
data class SimpleRequestFail<T>(val reason: String) : SimpleRequestUiState<T>()
