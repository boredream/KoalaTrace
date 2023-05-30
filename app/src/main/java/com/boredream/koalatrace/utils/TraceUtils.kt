package com.boredream.koalatrace.utils

import com.amap.api.maps.AMap
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.PolygonHoleOptions
import com.amap.api.maps.model.PolygonOptions
import com.blankj.utilcode.util.TimeUtils
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.constant.LocationConstant
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.MultiPolygon
import org.locationtech.jts.geom.Polygon
import org.locationtech.jts.geom.util.GeometryCombiner
import org.locationtech.jts.operation.buffer.BufferOp
import org.locationtech.jts.operation.buffer.BufferParameters
import org.locationtech.jts.operation.overlay.OverlayOp
import org.locationtech.jts.operation.polygonize.Polygonizer
import org.locationtech.jts.operation.union.CascadedPolygonUnion.union
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier
import kotlin.math.pow


object TraceUtils {

    private val logger = PrintLogger()

    fun getTraceListName(traceList: ArrayList<TraceLocation>): String {
        val time = TimeUtils.millis2String(traceList[traceList.lastIndex].time)
        return "轨迹 $time"
    }

    fun calculateDuration(traceList: ArrayList<TraceLocation>): Long {
        return (System.currentTimeMillis() - traceList[0].time) / 1000
    }

    fun calculateDistance(traceList: ArrayList<TraceLocation>): Int {
        var totalDistance = 0f
        for (i in 1 until traceList.size) {
            val distance = AMapUtils.calculateLineDistance(
                LatLng(traceList[i - 1].latitude, traceList[i - 1].longitude),
                LatLng(traceList[i].latitude, traceList[i].longitude)
            )
            totalDistance += distance
        }
        return totalDistance.toInt()
    }

    /**
     * 是否为有效轨迹
     */
    fun isValidTrace(record: TraceRecord): Boolean {
        // 总轨迹点数量<xx无效
        if (record.traceList.size < LocationConstant.SAVE_TRACE_MIN_POSITION_SIZE) return false
        // 总距离<xx米无效
        if (record.distance < LocationConstant.SAVE_TRACE_MIN_DISTANCE) return false
        return true
    }

    /**
     * 获取离目标经纬度，在范围内最近的点
     */
    fun getMostNearlyLocation(
        traceList: ArrayList<TraceLocation>,
        latitude: Double,
        longitude: Double,
        zoomLevel: Float,
    ): TraceLocation? {
        // 范围需要根据比例尺计算
        val maxZoomLevel = 20.0
        val ratio = 2.0.pow(maxZoomLevel - zoomLevel)
        val range = 5 * ratio

        // 范围内最近的点位
        var minDistanceLocation: TraceLocation? = null
        // 最近的
        var minDistance = Float.MAX_VALUE
        traceList.forEach {
            val distance = AMapUtils.calculateLineDistance(
                LatLng(latitude, longitude),
                LatLng(it.latitude, it.longitude)
            )
            if (distance < range && distance < minDistance) {
                minDistance = distance
                minDistanceLocation = it
            }
        }
        logger.i("latitude = $latitude, longitude = $longitude, zoomLevel = $zoomLevel, range = $range")
        return minDistanceLocation
    }

    fun drawTraceListLineBuffer(
        map: AMap,
        recordList: List<TraceRecord>,
        color: Int
    ) {
        // 挨个生成line buffer
        val polygonList = arrayListOf<Polygon>()
        recordList.forEach {
            val geometry = simpleLine(it.traceList)
            val lineBufferPolygon = createLineBuffer(geometry)
            polygonList.add(lineBufferPolygon)
        }

        // 合并
        if(polygonList.size == 0) return
        var polygon : Geometry = GeometryFactory().createPolygon()
        val start = System.currentTimeMillis()
        polygonList.forEach { polygon = polygon.union(it) }
        logger.i("combine polygon duration ${System.currentTimeMillis() - start}")

        // 多个路线拼接的图，可能合并成一个形状，也可能是分开的几个形状，需要各自单独绘制
        if(polygon is Polygon) {
            drawJstPolygon(map, polygon as Polygon, color)
        } else if(polygon is MultiPolygon) {
            val polygonizer = Polygonizer()
            polygonizer.add(polygon)
            polygonizer.polygons.forEach {
                logger.i("merge geometry from ${polygonList.size} to ${polygonizer.polygons.size}")
                drawJstPolygon(map, it as Polygon, color)
            }
            logger.i("merge geometry from ${polygonList.size} to ${polygonizer.polygons.size}")
        }
    }

    private fun simpleLine(traceList: ArrayList<TraceLocation>): Geometry {
        val start = System.currentTimeMillis()
        // 先经纬度转为jts的line对象
        val factory = GeometryFactory()
        val coordinateList = arrayListOf<Coordinate>()
        traceList.forEach { coordinateList.add(Coordinate(it.latitude, it.longitude)) }
        val line = factory.createLineString(coordinateList.toTypedArray())

        // 简化线的几何形状
        val tolerance = LocationConstant.ONE_METER_LAT_LNG * 20 // 简化容差
        val simplifier = DouglasPeuckerSimplifier(line)
        simplifier.setDistanceTolerance(tolerance)
        logger.i("simple line duration ${System.currentTimeMillis() - start}")
        return simplifier.resultGeometry
    }

    private fun createLineBuffer(line: Geometry): Polygon {
        // line-buffer 用线计算区域面积
        val start = System.currentTimeMillis()
        val bufferParams = BufferParameters()
        bufferParams.endCapStyle = BufferParameters.CAP_ROUND
        bufferParams.joinStyle = BufferParameters.JOIN_ROUND
        val bufferOp = BufferOp(line, bufferParams)
        val width = LocationConstant.ONE_METER_LAT_LNG * 50
        val polygon = bufferOp.getResultGeometry(width) as Polygon
        logger.i("line buffer duration ${System.currentTimeMillis() - start}")
        return polygon
    }

    private fun drawJstPolygon(
        map: AMap,
        polygon: Polygon,
        color: Int
    ) {
        val start = System.currentTimeMillis()
        // 注意环的情况
        val polygonOptions = PolygonOptions()
            .addAll(polygon.exteriorRing.coordinates.map { LatLng(it.x, it.y) })
            .fillColor(color)
            .strokeWidth(0f)

        if (polygon.numInteriorRing > 0) {
            // TODO: 环如果过小，可以省略
            for (index in 0 until polygon.numInteriorRing) {
                logger.i("draw polygon hole = $index")
                val inter = polygon.getInteriorRingN(index).coordinates.map { LatLng(it.x, it.y) }
                polygonOptions.addHoles(PolygonHoleOptions().addAll(inter))
            }
        }
        map.addPolygon(polygonOptions)
        logger.i("addJstPolygon duration ${System.currentTimeMillis() - start}")
    }

}