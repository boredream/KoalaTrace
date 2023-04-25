package com.boredream.koalatrace.data.constant

import android.text.format.DateUtils

object LocationConstant {

    // TODO: 合理的阈值，多采集数据 vs 节省电量

    /**
     * 经纬度大概1米距离的粒度
     */
    const val ONE_METER_LAT_LNG = 0.00001

    /**
     * 定位间隔（秒）
     */
//    const val LOCATION_INTERVAL: Long = 5000L
    const val LOCATION_INTERVAL: Long = 3000L // test

    /**
     * 停留时间阈值（秒），超过这个时间后会自动记录一笔定位数据
     */
//    const val STOP_THRESHOLD_DURATION: Long = DateUtils.MINUTE_IN_MILLIS * 10
    const val STOP_THRESHOLD_DURATION: Long = DateUtils.SECOND_IN_MILLIS * 10 // test

    /**
     * 最小计算追踪点，单位米
     */
    const val TRACE_DISTANCE_THRESHOLD = 2

    /**
     * 最小保存轨迹点数量，小于等于时删除
     */
    const val SAVE_TRACE_MIN_POSITION_SIZE = 1

    /**
     * 判断设备移动的加速度阈值（单位：m/s²）
     */
    const val DETERMINE_MOVEMENT_THRESHOLD = 0.5

    /**
     * 判断设备移动的加速度，判断间隔
     */
//    const val DETERMINE_MOVEMENT_CHECK_INTERVAL = DateUtils.SECOND_IN_MILLIS * 30
    const val DETERMINE_MOVEMENT_CHECK_INTERVAL = DateUtils.SECOND_IN_MILLIS * 5 // test

}