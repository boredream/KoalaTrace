package com.boredream.koalatrace.data.repo

import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.constant.LocationConstant
import com.boredream.koalatrace.data.constant.LocationConstant.MIN_VALID_DISTANCE
import com.boredream.koalatrace.data.repo.source.LocationDataSource
import com.boredream.koalatrace.utils.Logger
import com.boredream.koalatrace.utils.TraceFilter
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 定位数据。这里的追踪数据，只针对定位SDK返回的单次追踪信息
 */
@Singleton
class LocationRepository @Inject constructor(
    private val logger: Logger,
    private val dataSource: LocationDataSource
) {

    companion object {
        const val STATUS_IDLE = 0
        const val STATUS_LOCATION = 1
        const val STATUS_TRACE = 2

        // 精度可信情况-完全可信
        const val ACCURACY_TYPE_TOTALLY_CREDIBLE = 1
        // 精度可信情况-可信
        const val ACCURACY_TYPE_CREDIBLE = 2
        // 精度可信情况-不可信
        const val ACCURACY_TYPE_UN_CREDIBLE = 3
    }

    var status = STATUS_IDLE
        set(value) {
            field = value
            onStatusChange.forEach { it.invoke(value) }
        }
    private var onStatusChange: LinkedList<(status: Int) -> Unit> = LinkedList()
    fun addStatusChangeListener(listener: (status: Int) -> Unit) {
        onStatusChange.add(listener)
    }

    fun removeStatusChangeListener(listener: (status: Int) -> Unit) {
        onStatusChange.remove(listener)
    }

    // 定位
    var myLocation: TraceLocation? = null
    private var onLocationSuccess: LinkedList<(location: TraceLocation) -> Unit> = LinkedList()
    fun addLocationSuccessListener(listener: (location: TraceLocation) -> Unit) {
        onLocationSuccess.add(listener)
    }

    fun removeLocationSuccessListener(listener: (location: TraceLocation) -> Unit) {
        onLocationSuccess.remove(listener)
    }

    // 追踪
    var traceList: ArrayList<TraceLocation> = ArrayList()
    private lateinit var traceFilter: TraceFilter
    private var onTraceSuccess: LinkedList<(allTracePointList: ArrayList<TraceLocation>) -> Unit> =
        LinkedList()

    fun addTraceSuccessListener(listener: (allTracePointList: ArrayList<TraceLocation>) -> Unit) {
        onTraceSuccess.add(listener)
    }

    fun removeTraceSuccessListener(listener: (allTracePointList: ArrayList<TraceLocation>) -> Unit) {
        onTraceSuccess.remove(listener)
    }

    /**
     * 开始定位
     */
    fun startLocation() {
        if (status == STATUS_IDLE) {
            dataSource.startLocation(::onLocationSuccess)
            status = STATUS_LOCATION
        }
    }

    /**
     * 停止定位
     */
    fun stopLocation() {
        if (status == STATUS_LOCATION) {
            // 追踪依赖定位，只有非追踪状态才可关闭
            dataSource.stopLocation()
            status = STATUS_IDLE
        }
    }

    /**
     * 开始追踪
     */
    fun startTrace() {
        if (status == STATUS_LOCATION) {
            // 只有定位状态下才能开启追踪
            traceFilter = TraceFilter()
            status = STATUS_TRACE
        }
    }

    /**
     * 停止追踪
     */
    fun stopTrace() {
        if (status == STATUS_TRACE) {
            // 追踪状态退化到定位
            status = STATUS_LOCATION
        }
    }

    /**
     * 清除追踪数据
     */
    fun clearTraceList() {
        traceList.clear()
    }

    /**
     * 定位成功
     */
    fun onLocationSuccess(location: TraceLocation) {
        // logger.v("onLocationSuccess dataSource = ${dataSource.javaClass.simpleName}, location = $location")
        myLocation = location
        onLocationSuccess.forEach { it.invoke(location) }
        if (status == STATUS_TRACE) {
            appendTracePoint(location)
        }
    }

    /**
     * 添加定位轨迹追踪点
     */
    private fun appendTracePoint(location: TraceLocation) {
        var distanceValid = true
        var distance = 0f
        var maxDistance = 0L
        if(traceList.size > 0) {
            // 计算新的point和上一个定位point距离
            val lastPoint = traceList[traceList.lastIndex]
            distance = AMapUtils.calculateLineDistance(
                LatLng(lastPoint.latitude, lastPoint.longitude),
                LatLng(location.latitude, location.longitude)
            )
            // 最大距离是时间差值可以走出的最远距离
            maxDistance = LocationConstant.MAX_WALK_SPEED * (location.time - lastPoint.time) / 1000
            // 距离在范围内则有效
            distanceValid = distance <= maxDistance && distance > MIN_VALID_DISTANCE
        }

        // 精度可信程度
        var accuracyCredible = ACCURACY_TYPE_CREDIBLE
        try {
            val accuracy = location.extraData!!.split("_")[1].toFloat()
            accuracyCredible = if(accuracy < LocationConstant.TOTALLY_CREDIBLE_ACCURACY) {
                ACCURACY_TYPE_TOTALLY_CREDIBLE
            } else if(accuracy > LocationConstant.CREDIBLE_ACCURACY) {
                ACCURACY_TYPE_CREDIBLE
            } else {
                ACCURACY_TYPE_UN_CREDIBLE
            }
        } catch (_: Exception) {}

        // 完全可信精度 or 可信精度+合理距离，都视为有效数据
        val validate = accuracyCredible == ACCURACY_TYPE_TOTALLY_CREDIBLE ||
                (accuracyCredible == ACCURACY_TYPE_CREDIBLE && distanceValid)
        if (validate) {
            // 移动距离设置阈值，且不能超过最大值（过滤坐标漂移的数据）
            location.action = TraceLocation.ACTION_ADD

            // 如果是高精度+超过最大距离，则代表是坐地铁等情况的跳点
            if(distance > LocationConstant.DIVISION_DISTANCE) {
                location.action = TraceLocation.ACTION_NEW_RECORD
            }

            traceList.add(location)
        } else {
            // 无效数据，只更新最后一个位置时间
            traceList.last().action = TraceLocation.ACTION_UPDATE
            traceList.last().time = location.time
        }
        onTraceSuccess.forEach { it.invoke(traceList) }
        logger.v("trace success, size = ${traceList.size}, " +
                "distance = $distance, " +
                "maxDistance = $maxDistance, " +
                "accuracyCredible = $accuracyCredible, " +
                "validate = $validate, " +
                "location = $location")
    }

}