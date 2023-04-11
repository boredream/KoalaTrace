package com.boredream.koalatrace.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.amap.api.maps.AMap.OnCameraChangeListener
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.blankj.utilcode.util.LogUtils
import com.boredream.koalatrace.R
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.constant.CommonConstant
import com.boredream.koalatrace.utils.FileUtils
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.operation.buffer.BufferOp
import org.locationtech.jts.operation.buffer.BufferParameters
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier


/**
 * 追踪路线的地图
 */
class TraceMapView : MapView {

    private var zoomLevel = 17f
    private var myLocation: TraceLocation? = null
    private var myLocationMarker: Marker? = null
    private var startDrawIndex = 0

    var isFollowingMode = false
        set(value) {
            field = value
            // set true 时，先移动一次camera
            if (value) myLocation?.let { moveCamera(it) }
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        // 样式
        map?.let {
            it.uiSettings.isScaleControlsEnabled = false
            it.uiSettings.isZoomControlsEnabled = false
            // 高德自定义样式 https://geohub.amap.com/mapstyle
            // TODO: 道路还有箭头？
            val styleData = FileUtils.readBytesFromAssets(context, "mapstyle/style.data")
            val styleExtraData = FileUtils.readBytesFromAssets(context, "mapstyle/style_extra.data")
            val styleOptions = CustomMapStyleOptions()
                .setEnable(true)
                .setStyleData(styleData)
                .setStyleExtraData(styleExtraData)
            it.setCustomMapStyle(styleOptions)
            it.addOnCameraChangeListener(object : OnCameraChangeListener {
                override fun onCameraChange(position: CameraPosition?) {

                }

                override fun onCameraChangeFinish(position: CameraPosition?) {
                    position?.let { it -> zoomLevel = it.zoom }
                }

            })
            myLocationMarker = it.addMarker(MarkerOptions())
        }
    }

    private fun moveCamera(location: TraceLocation) {
        val position = CameraPosition.Builder()
            .target(LatLng(location.latitude, location.longitude))
            .zoom(zoomLevel)
            .build()
        map.moveCamera(CameraUpdateFactory.newCameraPosition(position))
        LogUtils.i(position)
    }

    private var isFirstSetMyLocation = true
    fun setMyLocation(location: TraceLocation) {
        myLocation = location
        myLocationMarker?.position = LatLng(location.latitude, location.longitude)
        if (isFirstSetMyLocation) {
            locateMe()
            isFirstSetMyLocation = false
        }
        LogUtils.d(map.cameraPosition.target)
    }

    fun locateMe() {
        myLocation?.let { moveCamera(it) }
    }

    /**
     * 绘制正在跟踪的轨迹线路
     */
    fun drawTraceList(allTracePointList: ArrayList<TraceLocation>) {
        if (startDrawIndex >= allTracePointList.size) {
            // 如果开始绘制的位置，超过了轨迹列表大小，代表错误数据or轨迹列表更换了，此次为无效绘制
            return
        }

        val pointList = ArrayList<LatLng>()
        // 从 startDrawIndex 开始绘制
        for (i in startDrawIndex until allTracePointList.size) {
            pointList.add(allTracePointList[i].toLatLng())
        }
        val line = drawLine(pointList)
        // 绘制完成后，更新 startDrawIndex
        val newIndex = allTracePointList.lastIndex
        println("drawTraceList $startDrawIndex to $newIndex")
        startDrawIndex = newIndex
    }

    /**
     * 绘制多条不会变化的线路
     */
    fun drawMultiFixTraceList(multiTracePointList: ArrayList<ArrayList<TraceLocation>>) {
        // TODO: 如何擦除已绘制路线？
        multiTracePointList.forEach { it ->
            val pointList = ArrayList<LatLng>()
            it.forEach { pointList.add(it.toLatLng()) }
            drawLine(
                pointList,
                traceLineColor = ContextCompat.getColor(context, R.color.colorPrimaryLight)
            )

            var start = System.currentTimeMillis()
            // 先经纬度转为jts的line对象
            val factory = GeometryFactory()
            val coordinateList = arrayListOf<Coordinate>()
            pointList.forEach { coordinateList.add(Coordinate(it.latitude, it.longitude))}
            val line = factory.createLineString(coordinateList.toTypedArray())

            // 简化线的几何形状
            val tolerance = CommonConstant.ONE_METER_LAT_LNG * 20 // 简化容差
            val simplifier = DouglasPeuckerSimplifier(line)
            simplifier.setDistanceTolerance(tolerance)
            val simplifiedLine: Geometry = simplifier.resultGeometry

            val simplePointList = simplifiedLine.coordinates.map { LatLng(it.x + CommonConstant.ONE_METER_LAT_LNG * 200, it.y) }
            drawLine(
                ArrayList(simplePointList),
                traceLineColor = ContextCompat.getColor(context, R.color.txt_oran)
            )
            LogUtils.i("drawMultiFixTraceList simple line duration ${System.currentTimeMillis() - start}")

            // 绘制区域
            // 计算线的缓冲区
            val bufferParams = BufferParameters()
            bufferParams.endCapStyle = BufferParameters.CAP_ROUND
            bufferParams.joinStyle = BufferParameters.JOIN_ROUND
            start = System.currentTimeMillis()
            val bufferOp = BufferOp(simplifiedLine, bufferParams)
            val width = CommonConstant.ONE_METER_LAT_LNG * 50
            val polygon  = bufferOp.getResultGeometry(width) as org.locationtech.jts.geom.Polygon
            LogUtils.i("drawMultiFixTraceList line buffer duration ${System.currentTimeMillis() - start}")

            // 注意环的情况
            val polygonOptions = PolygonOptions()
                .addAll(polygon.exteriorRing.coordinates.map { LatLng(it.x, it.y) })
                .fillColor(Color.argb(150, 255, 0, 0))
                .strokeWidth(0f)

            if(polygon.numInteriorRing > 0) {
                // TODO: 环如果过小，可以省略
                for(index in 0 until polygon.numInteriorRing) {
                    LogUtils.i("draw polygon hole = $index")
                    val inter = polygon.getInteriorRingN(index).coordinates.map { LatLng(it.x, it.y) }
                    polygonOptions.addHoles(PolygonHoleOptions().addAll(inter))
                }
            }
            start = System.currentTimeMillis()
            map.addPolygon(polygonOptions)
            LogUtils.i("drawMultiFixTraceList addPolygon duration ${System.currentTimeMillis() - start}")
        }
    }

    private fun drawLine(
        pointList: ArrayList<LatLng>,
        traceLineWidth: Float = 15f,
        traceLineColor: Int = ContextCompat.getColor(context, R.color.colorPrimary)
    ) {
        // TODO: 中途有多个点定位失败，然后走出很远距离后，再次定位成功（如坐地铁），应该分多条线绘制
        map.addPolyline(PolylineOptions().addAll(pointList).width(traceLineWidth).color(traceLineColor))
    }

    private fun TraceLocation.toLatLng(): LatLng {
        return LatLng(this.latitude, this.longitude)
    }
}