package com.boredream.koalatrace.data.dto

data class PageResultDto<T>(
    val current: Int,
    val records: List<T>,
)