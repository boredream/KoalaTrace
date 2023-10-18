package com.boredream.koalatrace.data.repo.source

import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import com.boredream.koalatrace.data.ResponseEntity
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.TraceRecordArea
import com.boredream.koalatrace.db.AppDatabase
import com.boredream.koalatrace.utils.Logger
import javax.inject.Inject
import kotlin.collections.ArrayList

class TraceRecordLocalDataSource @Inject constructor(
    private val logger: Logger,
    appDatabase: AppDatabase
) : TraceRecordDataSource {

    private val traceRecordDao = appDatabase.traceRecordDao()
    private val traceLocationDao = appDatabase.traceLocationDao()

    suspend fun getUnFinishTraceRecord(): ResponseEntity<ArrayList<TraceRecord>> {
        return try {
            val list = traceRecordDao.loadUnFinishTraceRecord()
            ResponseEntity.success(ArrayList(list))
        } catch (e: Exception) {
            ResponseEntity(null, 500, e.toString())
        }
    }

    suspend fun getNoAddressTraceRecord(): ResponseEntity<ArrayList<TraceRecord>> {
        return try {
            val list = traceRecordDao.loadNoAddressTraceRecord()
            ResponseEntity.success(ArrayList(list))
        } catch (e: Exception) {
            ResponseEntity(null, 500, e.toString())
        }
    }

    @Transaction
    override suspend fun add(data: TraceRecord): ResponseEntity<TraceRecord> {
        var insert: Long = -1
        try {
            insert = traceRecordDao.insertOrUpdate(data)
            logger.i("insert or update record $data")
        } catch (e: Exception) {
            //
        }

        if (insert <= 0) {
            return ResponseEntity(null, 500, "数据插入失败")
        }
        data.id = insert
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
        data.id = insert
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

    suspend fun getListByCondition(
        startTime: Long?,
        endTime: Long?,
        recordArea: TraceRecordArea?,
        needLocationList: Boolean,
    ): ResponseEntity<ArrayList<TraceRecord>> {
        return try {
            val queryBuilder = SupportSQLiteQueryBuilder.builder("TraceRecord")

            val selectionSql = StringBuilder()
            val selectionArgs = arrayListOf<Any>()

            // 基础条件
            selectionSql.append("isRecording = 0")

            if(startTime != null && endTime != null) {
                // 使用结束时间判断
                selectionSql.append(" AND (endTime BETWEEN ? AND ?)")
                selectionArgs.add(startTime)
                selectionArgs.add(endTime)
            }

            if (recordArea != null) {
                // 市 + 区
                selectionSql.append(" AND (subAdminArea = ? AND locality = ?)")
                selectionArgs.add(recordArea.subAdminArea ?: "")
                selectionArgs.add(recordArea.locality ?: "")
            }

            queryBuilder.selection(selectionSql.toString(), selectionArgs.toArray())
            queryBuilder.orderBy("startTime desc")
            val query = queryBuilder.create()
            logger.i(query.sql)

            val result = ArrayList<TraceRecord>()

            // 判断是否要关联查询轨迹下的坐标点
            if(needLocationList) {
                traceRecordDao.queryWithLocation(query).forEach {
                    it.record.traceList = ArrayList(it.locationList)
                    result.add(it.record)
                }
            } else {
                result.addAll(traceRecordDao.query(query))
            }

            ResponseEntity.success(result)
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

    suspend fun getNearbyRecordListWithLocation(
        targetLat: Double,
        targetLng: Double,
        range: Double
    ): ResponseEntity<ArrayList<TraceRecord>> {
        return try {
            val minLat = targetLat - range
            val maxLat = targetLat + range
            val minLng = targetLng - range
            val maxLng = targetLng + range

            val traceList = arrayListOf<TraceRecord>()
            val list = traceRecordDao.loadNearbyRecordWithLocation(minLat, maxLat, minLng, maxLng)
            list.forEach {
                val record = it.record
                record.traceList = ArrayList(it.locationList)
                traceList.add(record)
            }

            logger.i("minLat=$minLat, maxLat=$maxLat, minLng=$minLng, maxLng=$maxLng ... size=${traceList.size}")
            ResponseEntity.success(traceList)
        } catch (e: Exception) {
            ResponseEntity(null, 500, e.toString())
        }
    }

    suspend fun getNearbyLocationList(
        targetLat: Double,
        targetLng: Double,
        range: Double
    ): ResponseEntity<ArrayList<TraceLocation>> {
        return try {
            val minLat = targetLat - range
            val maxLat = targetLat + range
            val minLng = targetLng - range
            val maxLng = targetLng + range
            val list = traceLocationDao.loadNearby(minLat, maxLat, minLng, maxLng)
            ResponseEntity.success(ArrayList(list))
        } catch (e: Exception) {
            ResponseEntity(null, 500, e.toString())
        }
    }

    suspend fun getTraceLocationList(traceRecordDbId: Long): ResponseEntity<ArrayList<TraceLocation>> {
        return try {
            val list = traceLocationDao.loadByTraceRecordId(traceRecordDbId)
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
            delete = traceRecordDao.delete(data)
            logger.i("delete record $data")
        } catch (e: Exception) {
            //
        }

        if (delete <= 0) {
            return ResponseEntity(null, 500, "数据删除失败")
        }

        return try {
            // location跟着trace record走，不用于判断同步，所以直接删除
            val deleteListCount = traceLocationDao.deleteByTraceRecordId(data.id)
            logger.i("delete location list size = $deleteListCount")
            ResponseEntity.success(data)
        } catch (e: Exception) {
            ResponseEntity(null, 500, e.toString())
        }
    }

    @Transaction
    suspend fun deleteLocation(data: TraceLocation): ResponseEntity<TraceLocation> {
        var delete: Int = -1
        try {
            delete = traceLocationDao.delete(data)
            logger.i("delete location $data")
        } catch (e: Exception) {
            //
        }

        if (delete <= 0) {
            return ResponseEntity(null, 500, "数据删除失败")
        }
        return ResponseEntity.success(data)
    }

    suspend fun loadArea(): ResponseEntity<ArrayList<TraceRecordArea>> {
        return try {
            val list = traceRecordDao.loadArea()
            ResponseEntity.success(ArrayList(list))
        } catch (e: Exception) {
            ResponseEntity(null, 500, e.toString())
        }
    }

}