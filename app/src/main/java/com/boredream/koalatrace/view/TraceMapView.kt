package com.boredream.koalatrace.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.amap.api.maps.model.*
import com.boredream.koalatrace.R
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.constant.LocationConstant
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Polygon
import org.locationtech.jts.operation.buffer.BufferOp
import org.locationtech.jts.operation.buffer.BufferParameters
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier


/**
 * 追踪路线的地图
 */
class TraceMapView : BaseTraceMapView {

    private var myLocation: TraceLocation? = null
    private var myLocationStyle = MyLocationStyle()
    private var curTraceRecord: TraceRecord? = null
    private var startDrawIndex = 0
    private var historyLineList: ArrayList<Polyline> = arrayListOf()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        // AMap https://a.amap.com/lbs/static/unzip/Android_Map_Doc/index.html
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER) // 连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(1000) // 设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.strokeWidth(0f) // 精度圈边框
        myLocationStyle.radiusFillColor(Color.TRANSPARENT) // 精度圈填充
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location)) // 我的位置图标
    }

    var isFollowingMode = false
        set(value) {
            field = value
            // set true 时，先移动一次camera
            if (value) myLocation?.let { moveCamera(it) }
        }

    fun setMyLocationEnable(enable: Boolean) {
        map?.let {
            it.myLocationStyle = myLocationStyle // 设置定位蓝点的Style
            it.isMyLocationEnabled = enable
        }
    }

    private var isFirstSetMyLocation = true
    fun setMyLocation(location: TraceLocation) {
        myLocation = location
        if (isFirstSetMyLocation) {
            locateMe()
            isFirstSetMyLocation = false
        }
    }

    fun locateMe() {
        myLocation?.let { moveCamera(it) }
    }

    /**
     * 绘制正在跟踪的轨迹线路
     */
    fun drawTraceRecord(traceRecord: TraceRecord) {
        if (traceRecord != curTraceRecord) {
            // 线路变化时，重新绘图
            curTraceRecord = traceRecord
            startDrawIndex = 0
        }

        val allTracePointList = traceRecord.traceList
        if (startDrawIndex >= allTracePointList.size) {
            // 如果开始绘制的位置，超过了轨迹列表大小，代表错误数据or轨迹列表更换了，此次为无效绘制
            return
        }

        val pointList = ArrayList<LatLng>()
        // 从 startDrawIndex 开始绘制
        for (i in startDrawIndex until allTracePointList.size) {
            pointList.add(allTracePointList[i].toLatLng())
        }
        if (pointList.size <= 1) return

        drawLine(pointList)
        // 绘制完成后，更新 startDrawIndex
        val newIndex = allTracePointList.lastIndex
        logger.v("drawTraceList $startDrawIndex to $newIndex")
        startDrawIndex = newIndex
    }

    /**
     * 绘制多条不会变化的线路
     */
    fun drawMultiFixTraceList(traceList: ArrayList<TraceRecord>) {
        historyLineList.forEach { it.remove() }
        historyLineList.clear()
        val color = ContextCompat.getColor(context, R.color.colorPrimaryLight)
        traceList.forEach { record ->
            val line = drawTraceList(record.traceList, traceLineColor = color)
            line?.let { historyLineList.add(it) }
        }
    }

    /**
     * 删除轨迹点，刷新绘制线路
     */
    fun updateLineByDeleteTraceLocation(location: TraceLocation) {

    }

    private fun simpleLine(pointList: ArrayList<LatLng>): List<LatLng> {
        val start = System.currentTimeMillis()
        // 先经纬度转为jts的line对象
        val factory = GeometryFactory()
        val coordinateList = arrayListOf<Coordinate>()
        pointList.forEach { coordinateList.add(Coordinate(it.latitude, it.longitude)) }
        val line = factory.createLineString(coordinateList.toTypedArray())

        // 简化线的几何形状
        val tolerance = LocationConstant.ONE_METER_LAT_LNG * 20 // 简化容差
        val simplifier = DouglasPeuckerSimplifier(line)
        simplifier.setDistanceTolerance(tolerance)
        val simplifiedLine: Geometry = simplifier.resultGeometry

        val simplePointList = simplifiedLine.coordinates.map { LatLng(it.x, it.y) }
        logger.i("simple line duration ${System.currentTimeMillis() - start}")
        return simplePointList
    }

    private fun drawLineBuffer(line: Geometry) {
        // 绘制区域
        // 计算线的缓冲区
        var start = System.currentTimeMillis()
        val bufferParams = BufferParameters()
        bufferParams.endCapStyle = BufferParameters.CAP_ROUND
        bufferParams.joinStyle = BufferParameters.JOIN_ROUND
        val bufferOp = BufferOp(line, bufferParams)
        val width = LocationConstant.ONE_METER_LAT_LNG * 50
        val polygon = bufferOp.getResultGeometry(width) as Polygon
        logger.i("line buffer duration ${System.currentTimeMillis() - start}")

        // 注意环的情况
        val polygonOptions = PolygonOptions()
            .addAll(polygon.exteriorRing.coordinates.map { LatLng(it.x, it.y) })
            .fillColor(Color.argb(150, 255, 0, 0))
            .strokeWidth(0f)

        if (polygon.numInteriorRing > 0) {
            // TODO: 环如果过小，可以省略
            for (index in 0 until polygon.numInteriorRing) {
                logger.i("draw polygon hole = $index")
                val inter = polygon.getInteriorRingN(index).coordinates.map { LatLng(it.x, it.y) }
                polygonOptions.addHoles(PolygonHoleOptions().addAll(inter))
            }
        }
        start = System.currentTimeMillis()
        map.addPolygon(polygonOptions)
        logger.i("addPolygon duration ${System.currentTimeMillis() - start}")
    }

    fun updateSelectPosition(position: Int) {
//        val latLng = LatLng(39.906901, 116.397972)
//        val marker = aMap.addMarker(MarkerOptions().position(latLng).title("北京").snippet("DefaultMarker"))
    }

}