package com.boredream.koalatrace.data.usecase

import com.blankj.utilcode.util.CollectionUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.boredream.koalatrace.base.BaseUseCase
import com.boredream.koalatrace.data.ResponseEntity
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.constant.GlobalConstant
import com.boredream.koalatrace.data.constant.LocationParam
import com.boredream.koalatrace.data.repo.LocationRepository
import com.boredream.koalatrace.data.repo.SensorRepository
import com.boredream.koalatrace.data.repo.TraceRecordRepository
import com.boredream.koalatrace.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Singleton
class TraceUseCase @Inject constructor(
    private val logger: Logger,
    private val locationParam: LocationParam,
    private val locationRepository: LocationRepository,
    private val traceRecordRepository: TraceRecordRepository,
    private val sensorRepository: SensorRepository,
    private val scope: CoroutineScope,
) : BaseUseCase() {

    var currentTraceRecord: TraceRecord? = null
    private val geocodeSearchingSet = HashSet<Long>()

    private fun getMyLocation() = locationRepository.myLocation

    fun isTracing() = locationRepository.status == LocationRepository.STATUS_TRACE

    private val onTraceSuccessListener: (allTracePointList: ArrayList<TraceLocation>) -> Unit = {
        scope.launch { onTraceSuccess(it) }
    }

    suspend fun onTraceSuccess(locationList: ArrayList<TraceLocation>) {
        // 判断是否有跳点，则新建轨迹
        if (locationList.size > 0 && locationList.last().action == TraceLocation.ACTION_NEW_RECORD) {
            stopTrace()
            startTrace()
            return
        }

        addLocation2currentRecord(locationList)

        val stop = checkStopTrace()
        if (stop) {
            // 如果停留时间过长，停止追踪了，则开启监听移动
            sensorRepository.startListenerMovement()
            // 如果此时在后台，定位也关闭
            if (!GlobalConstant.isForeground) {
                stopLocation()
            }
        }
    }

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
        if (locationRepository.status != LocationRepository.STATUS_LOCATION) {
            // 定位中才可以开始记录轨迹
            return
        }
        sensorRepository.movementListener = { if (it) determineMovement() }
        locationRepository.addTraceSuccessListener(onTraceSuccessListener)
        locationRepository.startTrace()

        // 开始就创建轨迹
        createTraceRecord()
    }

    private fun determineMovement() {
        sensorRepository.stopListenerMovement()
        startLocation()
        scope.launch { startTrace() }
    }

    suspend fun createTraceRecord() {
        val time = System.currentTimeMillis()
        val timeStr = TimeUtils.millis2String(time)
        val title = "轨迹 $timeStr"
        val traceRecord = TraceRecord(title, time, time, 0, isRecording = true)
        traceRecordRepository.insertOrUpdate(traceRecord)
        currentTraceRecord = traceRecord
        traceRecordUpdate.forEach { it.invoke(traceRecord) }
        logger.i("create trace $title")
    }

    suspend fun addLocation2currentRecord(list: ArrayList<TraceLocation>) {
        // TODO: 第一个点就漂移的问题
        val record = currentTraceRecord ?: return
        if (CollectionUtils.isEmpty(list)) return
        // list 是源数据，没有dbId，所以要和当前已记录数据 record.traceList 对比
        val lastLocation = list.last()
        if (lastLocation.action == TraceLocation.ACTION_ADD) {
            // 新增
            record.traceList.add(lastLocation)
        } else if (lastLocation.action == TraceLocation.ACTION_UPDATE) {
            // 最后一个location修改
            record.traceList.last().time = lastLocation.time
            record.traceList.last().extraData = lastLocation.extraData
        }
        lastLocation.action = TraceLocation.ACTION_NONE

        // TODO: 回调主要用于刷新ui，放在sql后影响性能？
        // TODO: 有没有可能之前某个location没有添加成功，那这里需要添加多个
        traceRecordRepository.insertOrUpdateLocation(record.id, lastLocation)
        traceRecordUpdate.forEach { it.invoke(record) }
    }

    /**
     * 更新所有未完成状态的轨迹（记录中的、数据有问题的等）
     * @return Boolean
     */
    suspend fun refreshUnFinishTrace(): Boolean {
        var hasUpdate = false
        val list = traceRecordRepository.getUnFinishTraceRecord()
        if (list.isSuccess()) {
            val unFinishList = list.getSuccessData().filter { it != currentTraceRecord }
            logger.i("updateAllUnFinishRecord ${unFinishList.size}")
            unFinishList.forEach {
                traceRecordRepository.updateByTraceList(it)
                hasUpdate = true
            }
        }
        return hasUpdate
    }

    /**
     * 更新所有地址为空轨迹的区域信息
     */
    suspend fun checkUpdateRecordArea(): Boolean {
        val traceRecordList = traceRecordRepository.getNoAddressTraceRecord()
        if (traceRecordList.isSuccess()) {
            val list = traceRecordList.getSuccessData()
            list.forEach {
                val locationListResponse = traceRecordRepository.getLocationList(it.id)
                if(locationListResponse.isSuccess() && CollectionUtils.isNotEmpty(locationListResponse.data)) {
                    it.traceList = locationListResponse.getSuccessData()
                    updateRecordArea(it)
                }
            }
            return !CollectionUtils.isEmpty(list)
        }
        return false
    }

    /**
     * 更新轨迹的区域信息
     */
    fun updateRecordArea(record: TraceRecord) {
        logger.i("start updateRecordArea = $record")
        if (record.traceList.size == 0) return
        if (!StringUtils.isEmpty(record.adminArea)) return
        if (geocodeSearchingSet.contains(record.id)) return
        val mid = record.traceList[record.traceList.size / 2]
        locationRepository.geocodeSearch(mid.latitude, mid.longitude) {
            scope.launch {
                geocodeSearchingSet.remove(record.id)
                record.country = it?.country
                record.adminArea = it?.province
                record.subAdminArea = it?.city
                record.locality = it?.district
                record.subLocality = it?.township
                traceRecordRepository.update(record)
                logger.i(
                    "record = $record , "
                            + "adminArea = ${record.adminArea}, subAdminArea = ${record.subAdminArea}, "
                            + "locality = ${record.locality}, subLocality = ${record.subLocality}"
                )
            }
        }
    }

    suspend fun checkStopTrace(): Boolean {
        val record = currentTraceRecord ?: return false
        val list = record.traceList
        if (list.size <= 1) {
            // 如果一直是一个坐标点，则代表重启过于敏感，目标还是没有移动，超过一个较低阈值后直接删除当前路线
            val stayFromStart = System.currentTimeMillis() - record.startTime
            if (stayFromStart >= locationParam.stopThresholdDuration) {
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
        if (stay >= locationParam.stopThresholdDuration) {
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
        traceRecordUpdate.forEach { it.invoke(record) }
        updateRecordArea(record)
        locationRepository.clearTraceList()
        currentTraceRecord = null
        locationRepository.removeTraceSuccessListener(onTraceSuccessListener)
    }

    /**
     * 获取所有历史轨迹
     */
    suspend fun getAllHistoryTraceRecordList(): ResponseEntity<ArrayList<TraceRecord>> {
        // TODO: 显示今天的，历史数据按日期判断？
        val myLocation = getMyLocation() ?: return ResponseEntity.notExistError()
        // TODO: 我的位置不停的变化，变化后如何处理？重新获取？
        val response = traceRecordRepository.getNearHistoryTraceList(
            myLocation.latitude,
            myLocation.longitude
        )
        if (response.isSuccess() && response.data != null) {
            response.data.forEach {
                val locationList = traceRecordRepository.getLocationList(it.id).data
                it.traceList = locationList ?: arrayListOf()
            }
            // 剃出当前轨迹
            response.data.remove(currentTraceRecord)
        }
        return response
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