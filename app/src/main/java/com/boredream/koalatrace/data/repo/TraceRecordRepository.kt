package com.boredream.koalatrace.data.repo

import com.boredream.koalatrace.base.BaseRepository
import com.boredream.koalatrace.data.ResponseEntity
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.constant.LocationConstant
import com.boredream.koalatrace.data.repo.source.ConfigLocalDataSource
import com.boredream.koalatrace.data.repo.source.ConfigLocalDataSource.Companion.DATA_SYNC_TIMESTAMP_KEY
import com.boredream.koalatrace.data.repo.source.TraceRecordLocalDataSource
import com.boredream.koalatrace.data.repo.source.TraceRecordRemoteDataSource
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
    private val configDataSource: ConfigLocalDataSource,
    private val remoteDataSource: TraceRecordRemoteDataSource,
    private val localDataSource: TraceRecordLocalDataSource,
) : BaseRepository() {

    suspend fun getList() = localDataSource.getList()

    suspend fun getNearHistoryTraceList(
        targetLat: Double,
        targetLng: Double
    ): ResponseEntity<ArrayList<TraceRecord>> {
        // 查询附近线路 5km
        val rangeMeter = 5000
        val range = LocationConstant.ONE_METER_LAT_LNG * rangeMeter
        val traceRecordList = localDataSource.getNearbyList(targetLat, targetLng, range)
        // 查询线路下所有轨迹
        if (traceRecordList.isSuccess()) {
            traceRecordList.getSuccessData().forEach {
                it.traceList = localDataSource.getTraceLocationList(it.dbId).data ?: arrayListOf()
            }
            logger.i("near 「${rangeMeter}米」 history list size = ${traceRecordList.getSuccessData().size}")
        }
        return traceRecordList
    }

    suspend fun getLocationList(traceRecordDbId: String?) =
        localDataSource.getTraceLocationList(traceRecordDbId)

    suspend fun insertOrUpdate(data: TraceRecord) = localDataSource.add(data)

    suspend fun insertOrUpdateLocation(traceRecordDbId: String, data: TraceLocation)
            : ResponseEntity<TraceLocation> {
        data.traceRecordId = traceRecordDbId
        return localDataSource.insertOrUpdateLocation(data)
    }

    suspend fun insertOrUpdateLocationList(
        traceRecordDbId: String,
        dataList: ArrayList<TraceLocation>
    )
            : ResponseEntity<ArrayList<TraceLocation>> {
        dataList.forEach { it.traceRecordId = traceRecordDbId }
        return localDataSource.insertOrUpdateLocationList(dataList)
    }

    /**
     * 更新同步全局时间戳，一般在本地数据更新成功后调用
     */
    private fun updateSyncTime(syncTimestamp: Long?) {
        val timestamp = syncTimestamp ?: return

        val localTimestamp = configDataSource.getLong(DATA_SYNC_TIMESTAMP_KEY)
        if (timestamp > localTimestamp) {
            // 如果数据同步时间比本地保存的新，替换之
            configDataSource.set(DATA_SYNC_TIMESTAMP_KEY, timestamp)
            logger.i("update syncTimestamp $timestamp")
        }
    }

    suspend fun update(data: TraceRecord): ResponseEntity<TraceRecord> {
        data.synced = false // 同步标志位，有修改的都需要设为false
        return localDataSource.update(data)
    }

    /**
     * 更新所有未完成状态的轨迹（记录中的、数据有问题的等）
     * @return Boolean
     */
    suspend fun updateAllUnFinishRecord(): Boolean {
        var hasUpdate = false
        val list = localDataSource.getUnFinishTraceRecord()
        if (list.isSuccess()) {
            logger.i("updateAllUnFinishRecord ${list.getSuccessData().size}")
            list.getSuccessData().forEach {
                it.traceList = localDataSource.getTraceLocationList(it.dbId).data ?: arrayListOf()
                updateByTraceList(it)
                hasUpdate = true
            }
        }
        return hasUpdate
    }

    suspend fun updateByTraceList(record: TraceRecord) {
        val locationList = record.traceList
        if (locationList.size <= LocationConstant.SAVE_TRACE_MIN_POSITION_SIZE) {
            delete(record)
        } else {
            record.endTime = locationList[locationList.lastIndex].time
            record.distance = TraceUtils.calculateDistance(locationList)
            record.isRecording = false
            insertOrUpdate(record)
            logger.i("update traceRecord: ${record.name} , distance = ${record.distance}")
        }
    }

    suspend fun delete(data: TraceRecord): ResponseEntity<TraceRecord> {
        data.synced = false // 同步标志位，有修改的都需要设为false
        return localDataSource.delete(data)
    }

}