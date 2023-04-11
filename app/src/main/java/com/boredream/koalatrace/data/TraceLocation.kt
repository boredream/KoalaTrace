package com.boredream.koalatrace.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.blankj.utilcode.util.TimeUtils
import com.boredream.koalatrace.base.BaseEntity

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = TraceRecord::class,
            parentColumns = ["dbId"],
            childColumns = ["traceRecordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["traceRecordId"])]
)
open class TraceLocation(
    var latitude: Double,
    var longitude: Double,
    var time: Long = System.currentTimeMillis()
) : BaseEntity(), java.io.Serializable {

    @PrimaryKey(autoGenerate = true)
    var dbId: Long = 0
    var traceRecordId: String? = null
    var extraData: String? = null

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