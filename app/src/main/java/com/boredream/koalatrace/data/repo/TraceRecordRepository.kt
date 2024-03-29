package com.boredream.koalatrace.data.repo

import com.boredream.koalatrace.base.BaseRepository
import com.boredream.koalatrace.data.ResponseEntity
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.TraceRecordArea
import com.boredream.koalatrace.data.constant.LocationConstant
import com.boredream.koalatrace.data.repo.source.TraceRecordLocalDataSource
import com.boredream.koalatrace.utils.Logger
import com.boredream.koalatrace.utils.TraceUtils
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 轨迹记录，针对的是整条轨迹线路
 */
@Singleton
class TraceRecordRepository @Inject constructor(
    private val logger: Logger,
    private val localDataSource: TraceRecordLocalDataSource,
) : BaseRepository() {

    private var areaList: ArrayList<TraceRecordArea> = arrayListOf()
    private var areaNeedUpdate = true

    suspend fun getList() = localDataSource.getList()

    suspend fun getListByCondition(
        startTime: Long? = null,
        endTime: Long? = null,
        recordArea: TraceRecordArea? = null,
        needLocationList: Boolean
    ) = localDataSource.getListByCondition(startTime, endTime, recordArea, needLocationList)

    /**
     * 获取目标经纬度范围内所有轨迹
     */
    suspend fun getNearHistoryTraceList(
        targetLat: Double,
        targetLng: Double,
        rangeMeter: Int = 5000
    ): ResponseEntity<ArrayList<TraceRecord>> {
        // 查询附近线路 5km
        val range = LocationConstant.ONE_METER_LAT_LNG * rangeMeter
        return localDataSource.getNearbyRecordListWithLocation(targetLat, targetLng, range)
    }

    suspend fun getLocationList(traceRecordDbId: Long) =
        localDataSource.getTraceLocationList(traceRecordDbId)

    suspend fun insertOrUpdate(data: TraceRecord) = localDataSource.add(data)

    suspend fun insertOrUpdateLocation(traceRecordDbId: Long, data: TraceLocation)
            : ResponseEntity<TraceLocation> {
        data.traceId = traceRecordDbId
        val response = localDataSource.insertOrUpdateLocation(data)
        if(response.isSuccess()) {
            areaNeedUpdate = true
        }
        return response
    }

    suspend fun update(data: TraceRecord) = localDataSource.update(data)

    suspend fun getUnFinishTraceRecord() = localDataSource.getUnFinishTraceRecord()

    suspend fun getNoAddressTraceRecord() = localDataSource.getNoAddressTraceRecord()

    suspend fun updateByTraceList(record: TraceRecord): ResponseEntity<TraceRecord> {
        val locationList = record.traceList
        record.distance = TraceUtils.calculateDistance(locationList)

        val notValidReason = TraceUtils.isValidTrace(record)
        return if (notValidReason != null) {
            logger.i("record not valid, reason = $notValidReason")
            delete(record)
        } else {
            record.startTime = locationList[0].time
            record.endTime = locationList[locationList.lastIndex].time
            record.isRecording = false
            insertOrUpdate(record)
        }
    }

    suspend fun delete(data: TraceRecord) = localDataSource.delete(data)
    suspend fun deleteLocation(data: TraceLocation)  = localDataSource.deleteLocation(data)

    suspend fun loadArea(): ResponseEntity<ArrayList<TraceRecordArea>> {
        if(areaNeedUpdate) {
            val response = localDataSource.loadArea()
            if(response.isSuccess()) {
                areaNeedUpdate = false
                areaList = response.getSuccessData()
            }
            return response
        }
        return ResponseEntity.success(areaList)
    }

}