package com.boredream.koalatrace.view

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.amap.api.maps.AMap.OnCameraChangeListener
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.TextureMapView
import com.amap.api.maps.model.BaseOverlay
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.CustomMapStyleOptions
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.Polygon
import com.amap.api.maps.model.PolygonOptions
import com.amap.api.maps.model.Polyline
import com.amap.api.maps.model.PolylineOptions
import com.amap.api.maps.model.TextOptions
import com.blankj.utilcode.util.CollectionUtils
import com.boredream.koalatrace.R
import com.boredream.koalatrace.data.ExploreAreaInfo
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.constant.MapConstant
import com.boredream.koalatrace.utils.FileUtils
import com.boredream.koalatrace.utils.Logger
import com.boredream.koalatrace.utils.TraceUtils
import java.text.DecimalFormat


open class ExploreMapView : RecordMapView {

    private val exploreOverlay = arrayListOf<BaseOverlay>()

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
            // 绘制边界
            blockInfo.actualBoundary.split("==").forEach {
                exploreOverlay.add(
                    map.addPolyline(
                        PolylineOptions()
                            .addAll(TraceUtils.str2LatLngList(it))
                            .color(Color.argb(255, 255, 0, 0))
                            .width(2f)
                            .zIndex(MapConstant.FROG_MAP_Z_INDEX + 1f)
                    )
                )
            }

            // 绘制探索信息
            val rectLatLngList = TraceUtils.str2LatLngList(blockInfo.rectBoundary)
            val center = LatLng(
                (rectLatLngList[0].latitude + rectLatLngList[2].latitude) / 2,
                (rectLatLngList[0].longitude + rectLatLngList[2].longitude) / 2
            )
            map.addText(
                TextOptions()
                    .position(center)
                    .text(format.format(blockInfo.explorePercent * 100) + "%")
                    .backgroundColor(Color.TRANSPARENT)
                    .fontColor(Color.RED)
                    .fontSize(25)
                    .typeface(Typeface.DEFAULT_BOLD)
                    .zIndex(MapConstant.FROG_MAP_Z_INDEX + 1f)
            )
        }
    }

}