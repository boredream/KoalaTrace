package com.boredream.koalatrace.data.usecase

import com.blankj.utilcode.util.CollectionUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.boredream.koalatrace.base.BaseUseCase
import com.boredream.koalatrace.data.ResponseEntity
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.repo.LocationRepository
import com.boredream.koalatrace.data.repo.TraceRecordRepository
import com.boredream.koalatrace.utils.TraceUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraceUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
    private val traceRecordRepository: TraceRecordRepository,
) : BaseUseCase() {

    private var currentTraceRecord: TraceRecord? = null

    fun getMyLocation() = locationRepository.myLocation

    fun isTracing() = locationRepository.status == LocationRepository.STATUS_TRACE

    /**
     * 开始定位
     */
    fun startLocation() {
        locationRepository.startLocation()
    }

    /**
     * 结束定位
     */
    fun stopLocation() {
        locationRepository.stopLocation()
    }

    /**
     * 开始追踪轨迹
     */
    suspend fun startTrace() {
        // 开始记录轨迹时，就先创建一个线路
        val time = System.currentTimeMillis()
        val timeStr = TimeUtils.millis2String(time)
        val title = "轨迹 $timeStr"
        val traceRecord = TraceRecord(title, time, time, 0, isRecording = true)
        val response = traceRecordRepository.add(traceRecord)
        if (response.isSuccess()) {
            // 开始追踪
            currentTraceRecord = response.getSuccessData()
            locationRepository.startTrace()
            LogUtils.i("create traceRecord: ${traceRecord.name}")
        }
    }

    suspend fun addLocation2currentRecord(locationList: ArrayList<TraceLocation>) {
        if (CollectionUtils.isEmpty(locationList)) return
        val record = currentTraceRecord ?: return
        val location = locationList.last()
        location.traceRecordId = record.dbId
        traceRecordRepository.insertOrUpdateLocation(location)
    }

    /**
     * 结束追踪轨迹，并更新数据
     */
    suspend fun stopTrace() {
        locationRepository.stopTrace()

        val locationList = locationRepository.traceList
        if (CollectionUtils.isEmpty(locationList)) return
        val record = currentTraceRecord ?: return

        record.endTime = locationList[locationList.lastIndex].time
        record.distance = TraceUtils.calculateDistance(locationList)
        traceRecordRepository.add(record)
        locationRepository.clearTraceList()
        LogUtils.i("stop and save traceRecord: ${record.name} , distance = ${record.distance}")
    }

    /**
     * 获取所有历史轨迹
     */
    suspend fun getAllHistoryTraceListRecord(): ResponseEntity<ArrayList<TraceRecord>> {
        val myLocation = getMyLocation() ?: return ResponseEntity.notExistError()
        // TODO: 我的位置不停的变化，变化后如何处理？重新获取？
        val recordList = traceRecordRepository.getNearHistoryTraceList(
            myLocation.latitude,
            myLocation.longitude
        )
        if (recordList.isSuccess() && recordList.data != null) {
            recordList.data.forEach {
                val locationList = traceRecordRepository.getLocationList(it.dbId).data
                it.traceList = locationList
            }
        }
        return recordList
    }

    // TODO: 回调适合用函数吗？
    fun addLocationSuccessListener(listener: (location: TraceLocation) -> Unit) {
        locationRepository.addLocationSuccessListener(listener)
    }

    fun removeLocationSuccessListener(listener: (location: TraceLocation) -> Unit) {
        locationRepository.removeLocationSuccessListener(listener)
    }

    fun addTraceSuccessListener(listener: (allTracePointList: ArrayList<TraceLocation>) -> Unit) {
        locationRepository.addTraceSuccessListener(listener)
    }

    fun removeTraceSuccessListener(listener: (allTracePointList: ArrayList<TraceLocation>) -> Unit) {
        locationRepository.removeTraceSuccessListener(listener)
    }

    fun addStatusChangeListener(listener: (status: Int) -> Unit) {
        locationRepository.addStatusChangeListener(listener)
    }

    fun removeStatusChangeListener(listener: (status: Int) -> Unit) {
        locationRepository.removeStatusChangeListener(listener)
    }

}