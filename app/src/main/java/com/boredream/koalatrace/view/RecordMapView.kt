package com.boredream.koalatrace.view

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.amap.api.maps.AMap.OnCameraChangeListener
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.TextureMapView
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.CustomMapStyleOptions
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.Polyline
import com.amap.api.maps.model.PolylineOptions
import com.boredream.koalatrace.R
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.utils.FileUtils
import com.boredream.koalatrace.utils.Logger


open class RecordMapView : BaseMapView {

    private var lineList: ArrayList<Polyline> = arrayListOf()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

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
            val line = drawFixTraceList(record.traceList, traceLineWidth, traceLineColor)
            line?.let { lineList.add(it) }
        }
    }

    /**
     * traceList 绘制线路
     */
    fun drawFixTraceList(
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
        logger.v("size = ${pointList.size}")
        return map.addPolyline(
            PolylineOptions().addAll(pointList).width(traceLineWidth).color(traceLineColor)
        )
    }

}