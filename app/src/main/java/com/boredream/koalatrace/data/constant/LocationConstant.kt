package com.boredream.koalatrace.data.constant

import android.text.format.DateUtils

object LocationConstant {

    /**
     * 定位间隔（秒）
     */
    var LOCATION_INTERVAL: Long = 5000L

    /**
     * 停留时间阈值（秒），超过这个时间后会自动记录一笔定位数据
     */
    var STOP_THRESHOLD_DURATION: Long = DateUtils.MINUTE_IN_MILLIS * 10

}