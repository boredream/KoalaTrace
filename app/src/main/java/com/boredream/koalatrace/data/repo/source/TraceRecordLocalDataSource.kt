package com.boredream.koalatrace.data.repo.source

import androidx.room.Transaction
import com.boredream.koalatrace.data.ResponseEntity
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.db.AppDatabase
import com.boredream.koalatrace.utils.Logger
import javax.inject.Inject

class TraceRecordLocalDataSource @Inject constructor(
    private val logger: Logger,
    appDatabase: AppDatabase
) : TraceRecordDataSource {

    private val traceRecordDao = appDatabase.traceRecordDao()
    private val traceLocationDao = appDatabase.traceLocationDao()

    suspend fun getRecordingTraceRecord(): ResponseEntity<ArrayList<TraceRecord>> {
        return try {
            val list = traceRecordDao.loadRecordingRecord()
            ResponseEntity.success(ArrayList(list))
        } catch (e: Exception) {
            ResponseEntity(null, 500, e.toString())
        }
    }

    suspend fun getUnSyncedTraceRecord(): ResponseEntity<ArrayList<TraceRecord>> {
        return try {
            val list = traceRecordDao.loadUnSynced()
            ResponseEntity.success(ArrayList(list))
        } catch (e: Exception) {
            ResponseEntity(null, 500, e.toString())
        }
    }

    @Transaction
    override suspend fun add(data: TraceRecord): ResponseEntity<TraceRecord> {
        var insert: Long = -1
        try {
            // 如果traceList有值 Room会自动处理 TraceLocation
            insert = traceRecordDao.insertOrUpdate(data)
        } catch (e: Exception) {
            //
        }

        if (insert <= 0) {
            return ResponseEntity(null, 500, "数据插入失败")
        }
        return ResponseEntity.success(data)
    }

    suspend fun insertOrUpdateLocation(data: TraceLocation): ResponseEntity<TraceLocation> {
        var insert: Long = -1
        try {
            insert = traceLocationDao.insertOrUpdate(data)
        } catch (e: Exception) {
            //
        }
        if (insert <= 0) {
            return ResponseEntity(null, 500, "数据插入失败")
        }
        return ResponseEntity.success(data)
    }

    suspend fun insertOrUpdateLocationList(dataList: ArrayList<TraceLocation>)
            : ResponseEntity<ArrayList<TraceLocation>> {
        var insert: List<Long> = arrayListOf()

        try {
            insert = traceLocationDao.insertOrUpdateAll(dataList)
        } catch (e: Exception) {
            //
        }
        if (insert.isEmpty() || insert.any { it <= 0 }) {
            return ResponseEntity(null, 500, "数据插入失败")
        }
        return ResponseEntity.success(dataList)
    }

    suspend fun getTraceRecordByDbId(dbId: String): ResponseEntity<TraceRecord?> {
        return try {
            ResponseEntity.success(traceRecordDao.loadByDbId(dbId))
        } catch (e: Exception) {
            ResponseEntity(null, 500, e.toString())
        }
    }

    suspend fun getList(): ResponseEntity<ArrayList<TraceRecord>> {
        return try {
            ResponseEntity.success(ArrayList(traceRecordDao.loadAll()))
        } catch (e: Exception) {
            ResponseEntity(null, 500, e.toString())
        }
    }

    suspend fun getNearbyList(targetLat: Double, targetLng: Double, range: Double): ResponseEntity<ArrayList<TraceRecord>> {
        return try {
//            val range = AMapUtils.calculateLineDistance(
//                LatLng(targetLat, targetLng),
//                LatLng(targetLat, targetLng))
            val minLat = targetLat - range
            val maxLat = targetLat + range
            val minLng = targetLng - range
            val maxLng = targetLng + range
            val list = traceRecordDao.loadNearby(minLat, maxLat, minLng, maxLng)
//            val list = traceRecordDao.loadAll()
            logger.i("minLat=$minLat, maxLat=$maxLat, minLng=$minLng, maxLng=$maxLng")
            ResponseEntity.success(ArrayList(list))
        } catch (e: Exception) {
            ResponseEntity(null, 500, e.toString())
        }
    }

    suspend fun getTraceLocationList(traceRecordDbId: String?): ResponseEntity<ArrayList<TraceLocation>> {
        return try {
            val list = if(traceRecordDbId == null) {
                traceLocationDao.loadAll()
            } else {
                traceLocationDao.loadByTraceRecordId(traceRecordDbId)
            }
            ResponseEntity.success(ArrayList(list))
        } catch (e: Exception) {
            ResponseEntity(null, 500, e.toString())
        }
    }

    override suspend fun update(data: TraceRecord): ResponseEntity<TraceRecord> {
        var update: Int = -1
        try {
            update = traceRecordDao.update(data)
        } catch (e: Exception) {
            //
        }
        if (update <= 0) {
            return ResponseEntity(null, 500, "数据更新失败")
        }
        return ResponseEntity.success(data)
    }

    @Transaction
    override suspend fun delete(data: TraceRecord): ResponseEntity<TraceRecord> {
        var delete: Int = -1
        try {
            data.isDelete = true // 软删除，方便同步用
            delete = traceRecordDao.update(data)
            logger.i("delete record ${data.name}")
        } catch (e: Exception) {
            //
        }

        if (delete <= 0) {
            return ResponseEntity(null, 500, "数据删除失败")
        }

        return try {
            // location跟着trace record走，不用于判断同步，所以直接删除
            val deleteListCount = traceLocationDao.deleteByTraceRecordId(data.dbId)
            logger.i("delete location list size = $deleteListCount")
            ResponseEntity.success(data)
        } catch (e: Exception) {
            ResponseEntity(null, 500, e.toString())
        }
    }

}