package com.boredream.koalatrace.data.event

data class DataUpdateEvent(
    val dataClass: Class<Any>
)