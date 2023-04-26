package com.boredream.koalatrace.data.constant

object LocationConstant {

    // TODO: 合理的阈值，多采集数据 vs 节省电量

    /**
     * 经纬度大概1米距离的粒度
     */
    const val ONE_METER_LAT_LNG = 0.00001

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

}