package com.boredream.koalatrace.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.blankj.utilcode.util.TimeUtils
import com.boredream.koalatrace.base.BaseEntity
import com.boredream.koalatrace.base.BaseListData

/**
 * 轨迹记录
 */
@Entity
data class TraceRecord(
    var name: String,
    var startTime: Long,
    var endTime: Long,
    var distance: Int, // 单位米
    var country: String? = null, // 国家
    var adminArea: String? = null, // 表示一级行政区划名称，例如：“广东省”。
    var subAdminArea: String? = null, // 表示二级行政区划名称，例如：“深圳市”。
    var locality: String? = null, // 表示市级行政区划名称，例如：“福田区”。
    var subLocality: String? = null, // 表示区县级行政区划名称，例如：“华强北街道”。
    var isRecording: Boolean = false // 正在记录中
) : BaseEntity(), BaseListData {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @Ignore
    var traceList: ArrayList<TraceLocation> = arrayListOf()

    fun formatAddress(): String {
        val sb = StringBuilder()
        adminArea?.let { sb.append("-").append(it) }
        subAdminArea?.let { sb.append("-").append(it) }
        locality?.let { sb.append("-").append(it) }
        subLocality?.let { sb.append("-").append(it)}
        return if(sb.isNotEmpty()) {
            sb.substring(1)
        } else {
            ""
        }
    }

    fun getTimeRangeInfo(): String {
        return TimeUtils.millis2String(startTime, "[EEE] yyyy-MM-dd HH:mm:ss") + " ~ " + TimeUtils.millis2String(endTime, "HH:mm:ss")
    }

    override fun getItemId() = id

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TraceRecord
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "TraceRecord(id=$id, name='$name', startTime=$startTime, endTime=$endTime, distance=$distance)"
    }


}