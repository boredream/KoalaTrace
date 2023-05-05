package com.boredream.koalatrace.data.constant

import android.text.format.DateUtils

data class LocationParam(
    val locationInterval: Long = 5000L, // 定位间隔（秒）
    val stopThresholdDuration: Long = DateUtils.MINUTE_IN_MILLIS * 10, // 停留时间阈值（秒），超过后会自动停止
    val determineMoveCheckInterval: Long = DateUtils.SECOND_IN_MILLIS * 10, // 用传感器判断设备移动，判断周期
)