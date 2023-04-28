package com.boredream.koalatrace.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*
import kotlin.collections.ArrayList

/**
 * 轨迹记录
 */
@Entity
data class TraceRecord(
    var name: String,
    var startTime: Long,
    var endTime: Long,
    var distance: Int, // 单位米
    var detail: String? = null,
    var traceListStr: String? = null,
    var synced: Boolean = false, // 是否已经同步到服务器
    var isDelete: Boolean = false, // 软删除
    var syncTimestamp: Long? = null, // 同步数据的时间
    var isRecording: Boolean = false, // 正在记录中
    @PrimaryKey var dbId: String = UUID.randomUUID().toString()
) : Belong2UserEntity(), java.io.Serializable {

    @Ignore
    var traceList: ArrayList<TraceLocation> = arrayListOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TraceRecord
        if (dbId != other.dbId) return false
        return true
    }

    override fun hashCode(): Int {
        return dbId.hashCode()
    }

    override fun toString(): String {
        return "TraceRecord(dbId='$dbId', name='$name')"
    }

}