package com.boredream.koalatrace

import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.constant.LocationConstant
import com.boredream.koalatrace.data.constant.LocationParam

object TestDataConstants {

    val locationParam = LocationParam()

    fun getTraceList(): ArrayList<TraceLocation> {
        val traceList = ArrayList<TraceLocation>()
        for (i in 0..10) {
            traceList.add(getStepTraceLocation())
        }
        return traceList
    }

    private var step = 0
    fun getStepTraceLocation(): TraceLocation {
        val extra = step * LocationConstant.ONE_METER_LAT_LNG * 5
        val time = System.currentTimeMillis() + step * locationParam.locationInterval
        step += 1
        return getTraceLocation(latExtra = extra, time = time)
    }

    fun getTraceLocation(
        latExtra: Double = 0.0,
        lngExtra: Double = 0.0,
        time: Long = System.currentTimeMillis()
    ): TraceLocation {
        return TraceLocation(
            latitude = 31.227792 + latExtra,
            longitude = 121.355379 + lngExtra,
            time = time
        )
    }

}