package com.boredream.koalatrace.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
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
    var isRecording: Boolean = false // 正在记录中
) : BaseEntity(), BaseListData {

    override fun getItemId() = id

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @Ignore
    var traceList: ArrayList<TraceLocation> = arrayListOf()

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