package com.boredream.koalatrace.data

import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.boredream.koalatrace.base.BaseEntity
import org.locationtech.jts.geom.Polygon

/**
 * 区域探索信息 - 区块
 */
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

    @Ignore
    var explorePolygon: List<Polygon> = emptyList()

    override fun toString(): String {
        return "ExploreBlockInfo(, id=$id, areaCode='$areaCode', rectBoundary='$rectBoundary', explorePercent=$explorePercent, actualBoundary='$actualBoundary', actualArea=$actualArea)"
    }


}