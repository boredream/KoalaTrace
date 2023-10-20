package com.boredream.koalatrace.data

import androidx.room.*
import com.amap.api.maps.model.LatLng
import com.blankj.utilcode.util.TimeUtils
import com.boredream.koalatrace.base.BaseEntity

/**
 * 区域探索信息 - 区块
 */
@Entity
open class ExploreBlockInfo(
    val areaCode: String, // 所属区域
    val rectBoundary: String, // 区块的方形边界 经纬度用;分割
    val actualBoundary: String, // 区块的实际边界，是方形边界和行政区划取的交集，且可能是多个(用==分割)
    val actualArea: Double, // 区块实际面积
) : BaseEntity() {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    // 探索度
    var explorePercent: Float = 0.0f

}