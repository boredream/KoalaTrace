package com.boredream.koalatrace.data.repo.source

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

}