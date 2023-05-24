package com.boredream.koalatrace.data.constant

object LocationConstant {

    // TODO: 合理的阈值，多采集数据 vs 节省电量

    /**
     * 经纬度大概1米距离的粒度
     */
    const val ONE_METER_LAT_LNG = 0.00001

    /**
     * 分割距离，超过后自动分为俩线路（单位：m）
     */
    const val DIVISION_DISTANCE = 500

    /**
     * 最大行走速度（单位：m/s）
     */
    const val MAX_WALK_SPEED = 5

    /**
     * 最小计算追踪点（单位：m）
     */
    const val MIN_VALID_DISTANCE = 2f

    /**
     * 最小保存轨迹距离，小于等于时删除（单位：m）
     */
    const val SAVE_TRACE_MIN_DISTANCE = 20

    /**
     * 最小保存轨迹点数量，小于等于时删除
     */
    const val SAVE_TRACE_MIN_POSITION_SIZE = 2

    /**
     * 判断设备移动的加速度阈值（单位：m/s²）
     */
    const val DETERMINE_MOVEMENT_THRESHOLD = 0.5

    /**
     * 可信精度的阈值（单位：m）
     */
    const val CREDIBLE_ACCURACY = 30

    /**
     * 完全可信精度的阈值（单位：m）
     */
    const val TOTALLY_CREDIBLE_ACCURACY = 10

}