package com.boredream.koalatrace.data

import androidx.room.*
import com.blankj.utilcode.util.TimeUtils
import com.boredream.koalatrace.base.BaseEntity

@Entity
open class TraceLocation(
    var latitude: Double,
    var longitude: Double,
    var time: Long = System.currentTimeMillis()
) : BaseEntity() {

    companion object {
        const val ACTION_NONE = 0
        const val ACTION_ADD = 1
        const val ACTION_UPDATE = 2
        const val ACTION_NEW_RECORD = 3
    }

    @PrimaryKey(autoGenerate = true)
    var dbId: Long = 0
    var traceRecordId: String? = null
    var extraData: String? = null
    @Ignore
    var action: Int = ACTION_NONE

    override fun toString(): String {
        return "${TimeUtils.millis2String(time)}  $latitude,$longitude"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TraceLocation
        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false
        return true
    }

    override fun hashCode(): Int {
        var result = latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        return result
    }

}