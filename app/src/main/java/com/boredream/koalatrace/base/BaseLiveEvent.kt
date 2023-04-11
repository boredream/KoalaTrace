package com.boredream.koalatrace.base

open class BaseLiveEvent

data class ToastLiveEvent(
    val toast: String
) : BaseLiveEvent()
