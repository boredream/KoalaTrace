package com.boredream.koalatrace.data.repo.source

import com.boredream.koalatrace.data.ResponseEntity
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.net.ApiService
import javax.inject.Inject

class TraceRecordRemoteDataSource @Inject constructor(private val service: ApiService) :
    TraceRecordDataSource {

    override suspend fun add(data: TraceRecord): ResponseEntity<TraceRecord> {
        // 提交数据时，放在 traceListStr里，减少报文大小
        data.traceList?.let {
            val sb = StringBuilder()
            it.forEach {
                sb.append("_").append(it.time)
                    .append(",").append(it.latitude)
                    .append(",").append(it.longitude)
            }
            if (sb.isNotEmpty()) {
                data.traceListStr = sb.substring(1)
            }
            it.clear()
        }
        return try {
            service.addTraceRecord(data)
        } catch (e: Exception) {
            ResponseEntity.httpError(e)
        }
    }

    suspend fun getTraceLocationList(traceRecordId: String): ResponseEntity<ArrayList<TraceLocation>> {
        return try {
            service.getTraceLocationList(traceRecordId)
        } catch (e: Exception) {
            ResponseEntity(null, 500, e.toString())
        }
    }

    override suspend fun update(data: TraceRecord): ResponseEntity<TraceRecord> {
        return try {
            service.updateTraceRecord(data.id!!, data)
        } catch (e: Exception) {
            ResponseEntity.httpError(e)
        }
    }

    override suspend fun delete(data: TraceRecord): ResponseEntity<TraceRecord> {
        return try {
            service.deleteTraceRecord(data.id!!)
        } catch (e: Exception) {
            ResponseEntity.httpError(e)
        }
    }

    suspend fun getTraceRecordListBySyncTimestamp(localTimestamp: Long) =
        service.getTraceRecordListBySyncTimestamp(localTimestamp)

}