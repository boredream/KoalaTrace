package com.boredream.koalatrace.listener

interface OnCall<T> {
    fun call(t: T)
}