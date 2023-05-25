package com.boredream.koalatrace.data

data class TraceRecordArea(
    var subAdminArea: String? = null, // 表示二级行政区划名称，例如：“深圳市”。
    var locality: String? = null, // 表示市级行政区划名称，例如：“福田区”。
)