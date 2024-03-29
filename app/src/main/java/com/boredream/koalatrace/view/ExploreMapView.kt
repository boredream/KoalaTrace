package com.boredream.koalatrace.view

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.Polygon
import com.amap.api.maps.model.PolygonOptions
import com.amap.api.maps.model.Polyline
import com.amap.api.maps.model.PolylineOptions
import com.amap.api.maps.model.Text
import com.amap.api.maps.model.TextOptions
import com.boredream.koalatrace.R
import com.boredream.koalatrace.data.ExploreAreaInfo
import com.boredream.koalatrace.data.constant.MapConstant
import com.boredream.koalatrace.utils.TraceUtils
import java.text.DecimalFormat


open class ExploreMapView : RecordMapView {

    private val exploreOverlay = arrayListOf<Any>()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setBoundary(boundary: ArrayList<LatLng>) {
        map.addPolygon(
            PolygonOptions()
                .addAll(boundary)
                .fillColor(Color.argb(30, 0, 0, 255))
                .strokeWidth(0f)
        )
    }

    fun drawExploreArea(data: ExploreAreaInfo) {
        // 计算边界
        val builder = LatLngBounds.builder()
        data.boundaryLatLngList!!.forEach {
            builder.include(it)
        }
        val bounds = builder.build()
        val padding = MapConstant.MAP_PADDING // 地图边缘和轨迹之间的留白
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        map.moveCamera(cameraUpdate)

        exploreOverlay.forEach {
            if (it is Polygon) it.remove()
            else if (it is Polyline) it.remove()
            else if (it is Text) it.remove()
        }

        // 整个区域绘制迷雾，已探索区域挖孔
        exploreOverlay.addAll(
            TraceUtils.drawJstPolygonMask(
                map,
                data.boundaryLatLngList!!,
                data.explorePolygon,
                MapConstant.FROG_COLOR
            )
        )

        // 区域内每个区块
        val format = DecimalFormat("0.#")
        data.blockList.forEach { blockInfo ->
            val zLevel = MapConstant.FROG_MAP_Z_INDEX + 1f
            val isExploreLight = blockInfo.isExploreLight()

            // 绘制边界
            blockInfo.actualBoundary.split("==").forEach {
                val actualBoundaryLatLngList = TraceUtils.str2LatLngList(it)
                exploreOverlay.add(
                    map.addPolyline(
                        PolylineOptions()
                            .addAll(actualBoundaryLatLngList)
                            .color(resources.getColor(R.color.colorPrimary))
                            .width(2f)
                            .setDottedLine(true)
                            .zIndex(zLevel)
                    )
                )

                // 是否为点亮区域
                if(blockInfo.isExploreLight()) {
                    exploreOverlay.add(
                        map.addPolygon(
                            PolygonOptions()
                                .addAll(actualBoundaryLatLngList)
                                .fillColor(resources.getColor(R.color.a30gold))
                                .strokeWidth(0f)
                                .zIndex(zLevel)
                        )
                    )
                }
            }

            // 绘制探索信息
            val rectLatLngList = TraceUtils.str2LatLngList(blockInfo.rectBoundary)
            val center = LatLng(
                (rectLatLngList[0].latitude + rectLatLngList[2].latitude) / 2,
                (rectLatLngList[0].longitude + rectLatLngList[2].longitude) / 2
            )
            exploreOverlay.add(
                map.addText(
                    TextOptions()
                        .position(center)
                        .text(format.format(blockInfo.explorePercent * 100) + "%")
                        .backgroundColor(Color.TRANSPARENT)
                        .fontColor(resources.getColor(R.color.colorPrimary))
                        .fontSize(25)
                        .typeface(Typeface.DEFAULT_BOLD)
                        .zIndex(zLevel)
                )
            )
        }
    }

}