package com.boredream.koalatrace.data.usecase

import com.blankj.utilcode.util.CollectionUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.boredream.koalatrace.base.BaseUseCase
import com.boredream.koalatrace.data.ResponseEntity
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.constant.LocationConstant
import com.boredream.koalatrace.data.repo.LocationRepository
import com.boredream.koalatrace.data.repo.TraceRecordRepository
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

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
        if(locationRepository.status != LocationRepository.STATUS_LOCATION) {
            // 定位中才可以开始记录轨迹
            return
        }

        // 开始记录轨迹时，就先创建一个线路
        val time = System.currentTimeMillis()
        val timeStr = TimeUtils.millis2String(time)
        val title = "轨迹 $timeStr"
        val traceRecord = TraceRecord(title, time, time, 0, isRecording = true)
        val response = traceRecordRepository.insertOrUpdate(traceRecord)
        if (response.isSuccess()) {
            // 开始追踪
            currentTraceRecord = response.getSuccessData()
            locationRepository.startTrace()
            LogUtils.i("create traceRecord: ${traceRecord.name}")
        }
    }

    suspend fun addLocation2currentRecord(list: ArrayList<TraceLocation>) {
        if (CollectionUtils.isEmpty(list)) return
        val record = currentTraceRecord ?: return
        val location = list.last()
        location.traceRecordId = record.dbId
        record.traceList = list
        // TODO: 回调主要用于刷新ui，放在sql后影响性能？
        traceRecordRepository.insertOrUpdateLocation(location)
        traceRecordUpdate.forEach { it.invoke(record) }
    }

    suspend fun checkStopTrace(list: ArrayList<TraceLocation>) {
        if (list.size <= 1) return
        // 超过一个坐标点，查询最后一个距离上一个点位时间差，如果超过一个阈值，则代表停留在一个地方太久，直接保存并关闭轨迹记录
        val lastLocation = list[list.lastIndex]
        val lastPreLocation = list[list.lastIndex - 1]
        val stay = lastLocation.time - lastPreLocation.time
        if (stay >= LocationConstant.STOP_THRESHOLD_DURATION) {
            LogUtils.i("stay too long~")
            stopTrace()
            // TODO: 关闭定位后再次启动
//            startListenerMove()
//            traceUseCase.stopLocation()
        }
    }

    /**
     * 结束追踪轨迹，并更新数据
     */
    suspend fun stopTrace() {
        locationRepository.stopTrace()
        val record = currentTraceRecord ?: return
        traceRecordRepository.updateByTraceList(record)
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

    private var traceRecordUpdate: LinkedList<(traceRecord: TraceRecord) -> Unit> = LinkedList()
    fun addTraceRecordUpdateListener(listener: (traceRecord: TraceRecord) -> Unit) {
        traceRecordUpdate.add(listener)
    }
    fun removeTraceRecordUpdateListener(listener: (traceRecord: TraceRecord) -> Unit) {
        traceRecordUpdate.remove(listener)
    }


}