package com.boredream.koalatrace.utils

import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import com.blankj.utilcode.util.TimeUtils
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.constant.LocationConstant

object TraceUtils {

    fun getTraceListName(traceList: ArrayList<TraceLocation>): String {
        val time = TimeUtils.millis2String(traceList[traceList.lastIndex].time)
        return "轨迹 $time"
    }

    fun calculateDuration(traceList: ArrayList<TraceLocation>): Long {
        return (System.currentTimeMillis() - traceList[0].time) / 1000
    }

    fun calculateDistance(traceList: ArrayList<TraceLocation>): Int {
        var totalDistance = 0f
        for (i in 1 until traceList.size) {
            val distance = AMapUtils.calculateLineDistance(
                LatLng(traceList[i - 1].latitude, traceList[i - 1].longitude),
                LatLng(traceList[i].latitude, traceList[i].longitude)
            )
            totalDistance += distance
        }
        return totalDistance.toInt()
    }

    /**
     * 是否为有效轨迹
     */
    fun isValidTrace(record: TraceRecord): Boolean {
        // 总距离<xx米无效
        if(record.distance < LocationConstant.SAVE_TRACE_MIN_DISTANCE) return false
        // 总轨迹点数量<xx无效
        if(record.traceList.size < LocationConstant.SAVE_TRACE_MIN_POSITION_SIZE) return false
        return true
    }

}