package com.boredream.koalatrace.data.usecase

import com.boredream.koalatrace.base.BaseUseCase
import com.boredream.koalatrace.data.ResponseEntity
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.repo.TraceRecordRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraceDetailUseCase @Inject constructor(
    private val traceRecordRepository: TraceRecordRepository,
) : BaseUseCase() {

    private var traceRecordId: Long? = null

    fun init(traceRecordId: Long) {
        this.traceRecordId = traceRecordId
    }

    suspend fun getTraceList() = traceRecordRepository.getLocationList(traceRecordId!!)
    suspend fun updateRecord(data: TraceRecord) = traceRecordRepository.update(data)

    suspend fun deleteTraceLocation(data: TraceRecord, position: Int): ResponseEntity<TraceRecord> {
        if(data.traceList.size <= 2) {
            return ResponseEntity(null, 500, "轨迹点至少要保留两个")
        }

        // 先删除轨迹点
        val location = data.traceList[position]
        val response = traceRecordRepository.deleteLocation(location)
        if(!response.isSuccess()) {
            return ResponseEntity(data, response.code, response.msg)
        }
        // 然后更新轨迹线路信息
        data.traceList.removeAt(position)
        return traceRecordRepository.updateByTraceList(data)
    }

}