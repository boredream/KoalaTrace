package com.boredream.koalatrace.data.repo

import com.boredream.koalatrace.base.BaseRepository
import com.boredream.koalatrace.data.ResponseEntity
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
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

    suspend fun getList() = localDataSource.getList()

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
        val traceRecordList = localDataSource.getNearbyRecordList(targetLat, targetLng, range)
        // 查询线路下所有轨迹
        if (traceRecordList.isSuccess()) {
            traceRecordList.getSuccessData().forEach {
                it.traceList = localDataSource.getTraceLocationList(it.id).data ?: arrayListOf()
            }
            logger.i("near 「${rangeMeter}米」 history list size = ${traceRecordList.getSuccessData().size}")
        }
        return traceRecordList
    }

    suspend fun getLocationList(traceRecordDbId: Long) =
        localDataSource.getTraceLocationList(traceRecordDbId)

    suspend fun insertOrUpdate(data: TraceRecord) = localDataSource.add(data)

    suspend fun insertOrUpdateLocation(traceRecordDbId: Long, data: TraceLocation)
            : ResponseEntity<TraceLocation> {
        data.traceId = traceRecordDbId
        return localDataSource.insertOrUpdateLocation(data)
    }

    suspend fun insertOrUpdateLocationList(
        traceRecordDbId: Long,
        dataList: ArrayList<TraceLocation>
    ): ResponseEntity<ArrayList<TraceLocation>> {
        dataList.forEach { it.traceId = traceRecordDbId }
        return localDataSource.insertOrUpdateLocationList(dataList)
    }

    suspend fun update(data: TraceRecord) = localDataSource.update(data)

    suspend fun getUnFinishTraceRecord() = localDataSource.getUnFinishTraceRecord()

    suspend fun getNoAddressTraceRecord() = localDataSource.getNoAddressTraceRecord()

    suspend fun updateByTraceList(record: TraceRecord): ResponseEntity<TraceRecord> {
        val locationList = record.traceList
        record.distance = TraceUtils.calculateDistance(locationList)

        return if (!TraceUtils.isValidTrace(record)) {
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

}