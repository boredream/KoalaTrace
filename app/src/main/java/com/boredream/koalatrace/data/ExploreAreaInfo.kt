package com.boredream.koalatrace.data

import androidx.room.*
import com.blankj.utilcode.util.TimeUtils
import com.boredream.koalatrace.base.BaseEntity

/**
 * 区域探索信息
 */
@Entity
open class ExploreAreaInfo(
    val areaCode: String,
    val parentAreaCode: String,
) : BaseEntity() {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @Ignore
    var blockList: List<ExploreBlockInfo> = arrayListOf()

    // 探索度
    var explorePercent: Float = 0.0f

}