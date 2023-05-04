package com.boredream.koalatrace.data.usecase

import com.boredream.koalatrace.base.BaseUseCase
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

}