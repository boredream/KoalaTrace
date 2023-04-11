package com.boredream.koalatrace.data.repo.source

import com.boredream.koalatrace.data.ResponseEntity
import com.boredream.koalatrace.data.TraceRecord

interface TraceRecordDataSource {

    suspend fun add(data: TraceRecord): ResponseEntity<TraceRecord>

    suspend fun update(data: TraceRecord): ResponseEntity<TraceRecord>

    suspend fun delete(data: TraceRecord): ResponseEntity<TraceRecord>

}