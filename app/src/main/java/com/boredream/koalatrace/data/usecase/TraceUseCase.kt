package com.boredream.koalatrace.data.usecase

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
): BaseUseCase() {

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
    fun startTrace() {
        // 必须要先开始定位，先清除已有轨迹
        locationRepository.clearTraceList()
        locationRepository.startTrace()
    }

    /**
     * 结束追踪轨迹
     */
    fun stopTrace() {
        locationRepository.stopTrace()
    }

    /**
     * 保存追踪轨迹
     */
    suspend fun saveTraceRecord(): ResponseEntity<Boolean> {
        val traceList = locationRepository.traceList
        val title = TraceUtils.getTraceListName(traceList)
        val startTime = traceList[0].time
        val endTime = traceList[traceList.lastIndex].time
        val distance = TraceUtils.calculateDistance(traceList)
        val traceRecord = TraceRecord(title, startTime, endTime, distance)
        traceRecord.traceList = traceList

        traceRecordRepository.add(traceRecord)
        return ResponseEntity.success(true)
    }

    /**
     * 清除当前轨迹
     */
    fun clearTrace() {
        locationRepository.clearTraceList()
    }

    /**
     * 获取所有历史轨迹
     */
    suspend fun getAllHistoryTraceListRecord(): ResponseEntity<ArrayList<TraceRecord>> {
        val myLocation = getMyLocation() ?: return ResponseEntity.notExistError()
        // TODO: 我的位置不停的变化，变化后如何处理？重新获取？
        val recordList = traceRecordRepository.getNearHistoryTraceList(myLocation.latitude, myLocation.longitude)
        if(recordList.isSuccess() && recordList.data != null) {
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