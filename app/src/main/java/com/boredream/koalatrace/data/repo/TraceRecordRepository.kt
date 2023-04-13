package com.boredream.koalatrace.data.repo

import com.amap.api.mapcore.util.it
import com.blankj.utilcode.util.CollectionUtils
import com.boredream.koalatrace.base.BaseRepository
import com.boredream.koalatrace.data.ResponseEntity
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.constant.CommonConstant
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

    suspend fun syncDataPull() {
        // 服务端把本地时间戳之后的所有数据都查询出来，一次性返回给前端
        val localTimestamp = configDataSource.getLong(DATA_SYNC_TIMESTAMP_KEY)

        // 不关注 response
        try {
            val response = remoteDataSource.getTraceRecordListBySyncTimestamp(localTimestamp)
            response.data?.let { traceRecordList ->
                logger.i("pull data size = ${traceRecordList.size}")
                for (traceRecord in traceRecordList) {
                    // 1. 拉取的数据先和本地进行比较
                    val localResponse = localDataSource.getTraceRecordByDbId(traceRecord.dbId)
                    val localRecord = localResponse.data

                    // 2. 如果本地数据不存在，或者已经同步到服务器过了，直接覆盖
                    if (localRecord == null || localRecord.synced) {
                        // 还要拉取location list
                        traceRecord.id?.let {
                            traceRecord.traceList = remoteDataSource.getTraceLocationList(it).data
                        }
                        logger.i("pull and save data = ${traceRecord.name} , location size = ${traceRecord.traceList?.size}")
                        updateSyncTime(traceRecord.syncTimestamp)
                        insertOrUpdate(traceRecord)
                    } else {
                        // 3. 如果本地数据，同步标志位是false，即本地有修改还没提交给服务端的，处理冲突
                        // 冲突数据【本地的】，因为大部分是珍贵的轨迹收集数据，所以本地为准，服务端数据抛弃
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncDataPush() {
        localDataSource.getUnSyncedTraceRecord().data?.let { traceRecordList ->
            logger.i("getUnSyncedTraceRecord ${traceRecordList.size}")
            traceRecordList.forEach { pushDataToRemote(it) }
        }
    }

    suspend fun getList() = localDataSource.getList()

    suspend fun getNearHistoryTraceList(
        targetLat: Double,
        targetLng: Double
    ): ResponseEntity<ArrayList<TraceRecord>> {
        // 查询附近线路 5km
        val rangeMeter = 5000
        val range = CommonConstant.ONE_METER_LAT_LNG * rangeMeter
        val traceRecordList = localDataSource.getNearbyList(targetLat, targetLng, range)
        // 查询线路下所有轨迹
        if(traceRecordList.isSuccess()) {
            traceRecordList.getSuccessData().forEach {
                it.traceList = localDataSource.getTraceLocationList(it.dbId).data
            }
            logger.i("near 「${rangeMeter}米」 history list size = ${traceRecordList.getSuccessData().size}")
        }
        return traceRecordList
    }

    suspend fun getLocationList(traceRecordDbId: String) =
        localDataSource.getTraceLocationList(traceRecordDbId)

    suspend fun insertOrUpdate(data: TraceRecord) = localDataSource.add(data)

    suspend fun insertOrUpdateLocation(data: TraceLocation) = localDataSource.insertOrUpdate(data)

    suspend fun pushDataToRemote(data: TraceRecord): ResponseEntity<TraceRecord> {
        val response = if (data.id != null) {
            remoteDataSource.update(data)
        } else {
            // 如果是add新数据，同时要把location list数据也带着
            if (CollectionUtils.isEmpty(data.traceList)) {
                data.traceList = localDataSource.getTraceLocationList(data.dbId).data
            }
            remoteDataSource.add(data)
        }
        if (response.isSuccess() && response.data != null) {
            logger.i(
                "push success: ${response.data.name} , " +
                        "local update syncTimestamp = ${response.data.syncTimestamp}"
            )
            localDataSource.update(response.data)
            updateSyncTime(response.data.syncTimestamp)
        }
        return response
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

    suspend fun checkAllRecordUpdateByTraceList() : Boolean{
        var hasUpdate = false
        val list = localDataSource.getUnRecordingTraceRecord()
        if(list.isSuccess()) {
            logger.i("checkAllRecordUpdateByTraceList ${list.getSuccessData().size}")
            list.getSuccessData().forEach {
                it.traceList = localDataSource.getTraceLocationList(it.dbId).data
                updateByTraceList(it)
                hasUpdate = true
            }
        }
        return hasUpdate
    }

    suspend fun updateByTraceList(record: TraceRecord) {
        val locationList = record.traceList ?: return
        record.endTime = locationList[locationList.lastIndex].time
        record.distance = TraceUtils.calculateDistance(locationList)
        record.isRecording = false
        insertOrUpdate(record)
        logger.i("updateByTraceList ${record.name} , distance = ${record.distance}")
    }

    suspend fun delete(data: TraceRecord): ResponseEntity<TraceRecord> {
        data.synced = false // 同步标志位，有修改的都需要设为false
        return localDataSource.delete(data)
    }

}