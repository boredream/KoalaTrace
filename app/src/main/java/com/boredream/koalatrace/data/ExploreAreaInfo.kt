package com.boredream.koalatrace.data

import androidx.room.*
import com.amap.api.maps.model.LatLng
import com.blankj.utilcode.util.TimeUtils
import com.boredream.koalatrace.base.BaseEntity
import com.boredream.koalatrace.utils.TraceUtils
import com.google.gson.annotations.Expose
import org.locationtech.jts.geom.Polygon

/**
 * 区域探索信息
 */
open class ExploreAreaInfo(
    val areaCode: String,
    val parentAreaCode: String,
    val boundary: String,
) : BaseEntity() {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @Ignore
    var blockList: List<ExploreBlockInfo> = arrayListOf()

    @Ignore
    var explorePolygon: ArrayList<Polygon> = arrayListOf()

    // 探索度
    var explorePercent: Float = 0.0f

    @Transient
    var boundaryLatLngList: ArrayList<LatLng>? = null
        get() {
            if (field == null) {
                field = TraceUtils.str2LatLngList(boundary)
            }
            return field
        }

    override fun toString(): String {
        return "ExploreAreaInfo(id=$id, areaCode='$areaCode', parentAreaCode='$parentAreaCode', explorePercent=$explorePercent, blockList=$blockList)"
    }

}