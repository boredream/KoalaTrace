package com.boredream.koalatrace.data.repo.source

import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.*
import com.boredream.koalatrace.data.TraceLocation

/**
 * 定位数据源 - 定位SDK
 */
interface LocationDataSource {

    /**
     * 开始定位
     */
    fun startLocation(onSuccess: (location: TraceLocation) -> Unit)

    /**
     * 停止定位
     */
    fun stopLocation()

    fun geocodeSearch(latitude: Double, longitude: Double, callback: ((address: RegeocodeAddress?) -> Unit)?)

}