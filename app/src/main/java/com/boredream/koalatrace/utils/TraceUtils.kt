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
import org.locationtech.jts.algorithm.Orientation
import org.locationtech.jts.geom.*
import org.locationtech.jts.operation.buffer.BufferOp
import org.locationtech.jts.operation.buffer.BufferParameters
import org.locationtech.jts.operation.polygonize.Polygonizer
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
    ): List<com.amap.api.maps.model.Polygon> {
        // FIXME: 多条分开的线路时，绘制面+孔有问题，比如嘉定
        // TODO: 实际测试先union后buffer；和先buffer后union效率

        // 简化线路
        val lineList = arrayListOf <LineString>()
        recordList.forEach {
            lineList.add(simpleLine(it.traceList))
        }

        // merge line
        val mergeLine = GeometryFactory().createMultiLineString(lineList.toTypedArray())

        // line-buffer
        val mergePolygon = createLineBuffer(mergeLine)

        // 多个路线拼接的图，可能合并成一个形状，也可能是分开的几个形状，需要各自单独绘制
        val mapPolygonList = arrayListOf<com.amap.api.maps.model.Polygon>()
        if(mergePolygon is Polygon) {
            drawJstPolygon(map, mergePolygon, color)?.let { mapPolygonList.add(it) }
        } else if(mergePolygon is MultiPolygon) {
            val polygonizer = Polygonizer()
            polygonizer.add(mergePolygon)
            polygonizer.polygons.forEach { polygon ->
                drawJstPolygon(map, polygon as Polygon, color)?.let { mapPolygonList.add(it) }
            }
        }
        return mapPolygonList
    }

    fun simpleLine(traceList: ArrayList<TraceLocation>): LineString {
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
        return simplifier.resultGeometry as LineString
    }

    private fun createLineBuffer(line: Geometry): Geometry {
        // line-buffer 用线计算区域面积
        val start = System.currentTimeMillis()
        val bufferParams = BufferParameters()
        bufferParams.endCapStyle = BufferParameters.CAP_ROUND
        bufferParams.joinStyle = BufferParameters.JOIN_ROUND
        val bufferOp = BufferOp(line, bufferParams)
        val width = LocationConstant.ONE_METER_LAT_LNG * 50
        logger.i("line buffer duration ${System.currentTimeMillis() - start}")
        return bufferOp.getResultGeometry(width)
    }

    private fun drawJstPolygon(
        map: AMap,
        polygon: Polygon,
        color: Int
    ): com.amap.api.maps.model.Polygon? {
        val start = System.currentTimeMillis()

        // 外环
        val polygonOptions = PolygonOptions()
            .addAll(polygon.exteriorRing.coordinates.map { LatLng(it.x, it.y) })
            .fillColor(color)
            .strokeWidth(0f)

        // 内孔
        var polygonHoleOptions: PolygonHoleOptions? = null
        if (polygon.numInteriorRing > 0) {
            polygonHoleOptions = PolygonHoleOptions()
            for (index in 0 until polygon.numInteriorRing) {
                logger.i("add polygon hole = $index")
                var interRing = polygon.getInteriorRingN(index)

//                // TODO: 环如果过小，可以省略
//                val interRingPolygon = geometryFactory.createPolygon(interRing)
//                logger.i("interRingPolygon area = ${interRingPolygon.area}")

                // 高德地图对孔的要求是方向必须是逆时针的
                if(!Orientation.isCCW(interRing.coordinates)) {
                    // 如果是顺时针，则反转下
                    interRing = interRing.reverse()
                }
                val inter = interRing.coordinates.map { LatLng(it.x, it.y) }
                polygonHoleOptions.addAll(inter)
            }
        }
        polygonHoleOptions?.let { polygonOptions.addHoles(it) }

        logger.i("addJstPolygon duration ${System.currentTimeMillis() - start}")
        return map.addPolygon(polygonOptions)
    }

}