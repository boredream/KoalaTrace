package com.boredream.koalatrace.data

import androidx.room.Embedded
import androidx.room.Relation

data class TraceRecordWithLocation(

    @Embedded val record: TraceRecord,

    @Relation(
        parentColumn = "id",
        entityColumn = "traceId"
    )
    val locationList: List<TraceLocation>

)