package com.boredream.koalatrace.db.dao

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.TraceRecordArea
import com.boredream.koalatrace.data.TraceRecordWithLocation


@Dao
interface TraceRecordDao {

    @RawQuery(observedEntities = [TraceRecord::class])
    suspend fun query(query: SupportSQLiteQuery): List<TraceRecord>

    @RawQuery(observedEntities = [TraceRecordWithLocation::class])
    suspend fun queryWithLocation(query: SupportSQLiteQuery): List<TraceRecordWithLocation>

    @Query("SELECT DISTINCT subAdminArea, locality FROM TraceRecord WHERE subAdminArea IS NOT NULL AND locality IS NOT NULL GROUP BY subAdminArea, locality")
    suspend fun loadArea(): List<TraceRecordArea>

    @Query("SELECT * FROM TraceRecord WHERE subAdminArea IS NULL AND isRecording = 0")
    suspend fun loadNoAddressTraceRecord(): List<TraceRecord>

    @Query("SELECT * FROM TraceRecord WHERE isRecording = 1")
    suspend fun loadUnFinishTraceRecord(): List<TraceRecord>

    @Query("SELECT * FROM TraceRecord order by startTime desc")
    suspend fun loadAll(): List<TraceRecord>

    @Query("SELECT * FROM TraceRecord WHERE id = :id")
    suspend fun loadById(id: Long): TraceRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(traceRecord: TraceRecord): Long

    @Delete
    suspend fun delete(data: TraceRecord): Int

    @Query("DELETE FROM TraceRecord")
    suspend fun deleteAll(): Int

    @Update
    suspend fun update(data: TraceRecord): Int

    @Transaction
    @Query("SELECT * FROM TraceRecord WHERE id IN (SELECT DISTINCT traceId FROM TraceLocation WHERE " +
            "latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLng AND :maxLng)")
    suspend fun loadNearbyRecordWithLocation(
        minLat: Double, maxLat: Double,
        minLng: Double, maxLng: Double
    ): List<TraceRecordWithLocation>

}