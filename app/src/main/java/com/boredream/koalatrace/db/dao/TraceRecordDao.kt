package com.boredream.koalatrace.db.dao

import androidx.room.*
import com.boredream.koalatrace.data.TraceRecord


@Dao
interface TraceRecordDao {

    @Query("SELECT * FROM TraceRecord WHERE synced = 0")
    suspend fun loadUnSynced(): List<TraceRecord>

    @Query("SELECT * FROM TraceRecord WHERE isDelete = 0 AND (isRecording = 1 OR distance = 0)")
    suspend fun loadUnFinishTraceRecord(): List<TraceRecord>

    @Query("SELECT * FROM TraceRecord WHERE isDelete = 0 limit :limit offset :offset")
    suspend fun loadByPage(limit: Int, offset: Int): List<TraceRecord>

    @Query("SELECT * FROM TraceRecord WHERE isDelete = 0 order by startTime desc")
    suspend fun loadAll(): List<TraceRecord>

    @Query("SELECT * FROM TraceRecord WHERE dbId = :dbId")
    suspend fun loadByDbId(dbId: String): TraceRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(traceRecord: TraceRecord): Long

    @Delete
    suspend fun delete(data: TraceRecord): Int

    @Query("DELETE FROM TraceRecord")
    suspend fun deleteAll(): Int

    @Update
    suspend fun update(data: TraceRecord): Int

    @Query( "SELECT * FROM TraceRecord WHERE isDelete = 0 AND dbId IN (SELECT DISTINCT traceRecordId FROM TraceLocation WHERE " +
                "latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLng AND :maxLng)")
    suspend fun loadNearby(
        minLat: Double, maxLat: Double,
        minLng: Double, maxLng: Double
    ): List<TraceRecord>

}