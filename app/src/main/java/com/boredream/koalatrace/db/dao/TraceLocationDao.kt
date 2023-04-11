package com.boredream.koalatrace.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.boredream.koalatrace.data.TraceLocation

@Dao
interface TraceLocationDao {

    @Query("SELECT * FROM TraceLocation WHERE traceRecordId = :traceRecordDbId")
    suspend fun loadByTraceRecordId(traceRecordDbId: String): List<TraceLocation>

    @Insert
    suspend fun insertAll(dataList: List<TraceLocation>)

    @Delete
    suspend fun delete(data: TraceLocation)

    @Query("DELETE FROM TraceLocation WHERE traceRecordId = :traceRecordDbId")
    suspend fun deleteByTraceRecordId(traceRecordDbId: String): Int

}