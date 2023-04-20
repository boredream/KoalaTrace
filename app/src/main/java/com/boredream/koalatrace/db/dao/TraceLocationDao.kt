package com.boredream.koalatrace.db.dao

import androidx.room.*
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord

@Dao
interface TraceLocationDao {

    @Query("SELECT * FROM TraceLocation")
    suspend fun loadAll(): List<TraceLocation>

    @Query("SELECT * FROM TraceLocation WHERE traceRecordId = :traceRecordDbId")
    suspend fun loadByTraceRecordId(traceRecordDbId: String): List<TraceLocation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(traceRecord: TraceLocation): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(dataList: List<TraceLocation>): List<Long>

    @Delete
    suspend fun delete(data: TraceLocation)

    @Query("DELETE FROM TraceLocation WHERE traceRecordId = :traceRecordDbId")
    suspend fun deleteByTraceRecordId(traceRecordDbId: String): Int

}