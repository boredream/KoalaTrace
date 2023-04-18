package com.boredream.koalatrace.data.constant

import android.text.format.DateUtils

object LocationConstant {


    /**
     * 经纬度大概1米距离的粒度
     */
    const val ONE_METER_LAT_LNG = 0.00001

    /**
     * 定位间隔（秒）
     */
    const val LOCATION_INTERVAL: Long = 5000L

    /**
     * 停留时间阈值（秒），超过这个时间后会自动记录一笔定位数据
     */
    const val STOP_THRESHOLD_DURATION: Long = DateUtils.SECOND_IN_MILLIS * 5
//    const val STOP_THRESHOLD_DURATION: Long = DateUtils.MINUTE_IN_MILLIS * 10

    /**
     * 最小计算追踪点，单位米
     */
    const val TRACE_DISTANCE_THRESHOLD = 2

}