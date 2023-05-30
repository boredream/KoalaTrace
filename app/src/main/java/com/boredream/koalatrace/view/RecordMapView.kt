package com.boredream.koalatrace.view

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.amap.api.maps.AMap.OnCameraChangeListener
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.boredream.koalatrace.R
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.utils.FileUtils
import com.boredream.koalatrace.utils.Logger


open class RecordMapView : MapView {

    private var lineList: ArrayList<Polyline> = arrayListOf()
    protected val logger = Logger()
    var zoomLevel = 17f

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        // TODO: 地图样式问题，省略一些小区名字等细节，突出线路。参考百度的一蓑烟雨 https://lbsyun.baidu.com/customv2/editor/e5dea5db69354a92addfb8e2bf7115dd/lvyexianzong

        map?.let {
            it.uiSettings.isScaleControlsEnabled = false
            it.uiSettings.isZoomControlsEnabled = false
            // 高德自定义样式 https://geohub.amap.com/mapstyle
            val styleData = FileUtils.readBytesFromAssets(context, "mapstyle/style.data")
            val styleExtraData = FileUtils.readBytesFromAssets(context, "mapstyle/style_extra.data")
            val styleOptions = CustomMapStyleOptions()
                .setEnable(true)
                .setStyleData(styleData)
                .setStyleExtraData(styleExtraData)
            it.setCustomMapStyle(styleOptions)
            it.setRoadArrowEnable(false)
            it.addOnCameraChangeListener(object : OnCameraChangeListener {
                override fun onCameraChange(position: CameraPosition?) {

                }

                override fun onCameraChangeFinish(position: CameraPosition?) {
                    position?.let { it -> zoomLevel = it.zoom }
                }
            })
        }
    }

    protected fun moveCamera(location: TraceLocation) {
        val position = CameraPosition.Builder()
            .target(LatLng(location.latitude, location.longitude))
            .zoom(zoomLevel)
            .build()
        map.moveCamera(CameraUpdateFactory.newCameraPosition(position))
    }

    open fun clearLineList() {
        lineList.forEach { it.remove() }
        lineList.clear()
    }

    /**
     * 绘制多条不会变化的线路
     */
    open fun drawMultiFixTraceList(
        traceList: ArrayList<TraceRecord>,
        traceLineWidth: Float = 15f,
        traceLineColor: Int = ContextCompat.getColor(context, R.color.colorPrimary)
    ) {
        clearLineList()
        traceList.forEach { record ->
            val line = drawTraceList(record.traceList, traceLineWidth, traceLineColor)
            line?.let { lineList.add(it) }
        }
    }

    /**
     * traceList 绘制线路
     */
    fun drawTraceList(
        traceList: ArrayList<TraceLocation>,
        traceLineWidth: Float = 15f,
        traceLineColor: Int = ContextCompat.getColor(context, R.color.colorPrimary)
    ): Polyline? {
        val pointList = ArrayList<LatLng>()
        traceList.forEach { pointList.add(it.toLatLng()) }
        return drawLine(pointList, traceLineWidth, traceLineColor)
    }

    /**
     * 调整视角显示完整的轨迹列表
     */
    fun updateCamera2showCompleteTraceList(traceList: ArrayList<TraceLocation>) {
        // TODO: 数据库直接查询范围内边缘经纬度点

        // 计算边界
        val builder = LatLngBounds.builder()
        traceList.forEach {
            builder.include(it.toLatLng())
        }
        val bounds = builder.build()

        val padding = 100 // 地图边缘和轨迹之间的留白
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        map.moveCamera(cameraUpdate)
    }

    /**
     * 绘制线路
     */
    protected fun drawLine(
        pointList: ArrayList<LatLng>,
        traceLineWidth: Float = 15f,
        traceLineColor: Int = ContextCompat.getColor(context, R.color.colorPrimary)
    ): Polyline? {
        logger.i("size = ${pointList.size}")
        return map.addPolyline(
            PolylineOptions().addAll(pointList).width(traceLineWidth).color(traceLineColor)
        )
    }

    protected fun TraceLocation.toLatLng(): LatLng {
        return LatLng(this.latitude, this.longitude)
    }

}