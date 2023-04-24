package com.boredream.koalatrace.data.usecase

import com.blankj.utilcode.util.CollectionUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.boredream.koalatrace.base.BaseUseCase
import com.boredream.koalatrace.data.ResponseEntity
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.constant.GlobalConstant
import com.boredream.koalatrace.data.constant.LocationConstant
import com.boredream.koalatrace.data.repo.LocationRepository
import com.boredream.koalatrace.data.repo.TraceRecordRepository
import com.boredream.koalatrace.utils.Logger
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Singleton
class TraceUseCase @Inject constructor(
    private val logger: Logger,
    private val locationRepository: LocationRepository,
    private val traceRecordRepository: TraceRecordRepository,
) : BaseUseCase() {

    var currentTraceRecord: TraceRecord? = null

    private fun getMyLocation() = locationRepository.myLocation

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

        // TODO: 应该第一个定位点回来才开始创建轨迹

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
            logger.i("create traceRecord: ${traceRecord.name}")
        }
    }

    suspend fun addLocation2currentRecord() {
        val record = currentTraceRecord ?: return
        val list = locationRepository.traceList
        if (CollectionUtils.isEmpty(list)) return
        val location = list.last()
        record.traceList = list
        // TODO: 回调主要用于刷新ui，放在sql后影响性能？
        // TODO: 有没有可能之前某个location没有添加成功，那这里需要添加多个
        traceRecordRepository.insertOrUpdateLocation(record.dbId, location)
        traceRecordUpdate.forEach { it.invoke(record) }
    }

    suspend fun checkStopTrace() : Boolean {
        val record = currentTraceRecord ?: return false
        val list = record.traceList ?: return false
        if (list.size <= 1) {
            // 如果一直是一个坐标点，则代表重启过于敏感，目标还是没有移动，超过一个较低阈值后直接删除当前路线
            val stayFromStart = System.currentTimeMillis() - record.startTime
            if (stayFromStart >= LocationConstant.STOP_THRESHOLD_DURATION) {
                logger.i("stay too long~ delete trace")
                stopTrace()
                return true
            }
            return false
        }
        // 超过一个坐标点，查询最后一个距离上一个点位时间差，如果超过一个阈值，则代表停留在一个地方太久，直接保存并关闭轨迹记录
        val lastLocation = list[list.lastIndex]
        val lastPreLocation = list[list.lastIndex - 1]
        val stay = lastLocation.time - lastPreLocation.time
        if (stay >= LocationConstant.STOP_THRESHOLD_DURATION) {
            logger.i("stay too long~ save trace")
            stopTrace()
            return true
        }
        return false
    }

    /**
     * 结束追踪轨迹，并更新数据
     */
    suspend fun stopTrace() {
        locationRepository.stopTrace()
        val record = currentTraceRecord ?: return
        logger.i("stop trace: ${record.name}")
        traceRecordRepository.updateByTraceList(record)
        locationRepository.clearTraceList()
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