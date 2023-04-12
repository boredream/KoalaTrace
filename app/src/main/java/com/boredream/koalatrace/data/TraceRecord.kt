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
) : Belong2UserEntity() {

    @Ignore
    var traceList: ArrayList<TraceLocation>? = arrayListOf()

}