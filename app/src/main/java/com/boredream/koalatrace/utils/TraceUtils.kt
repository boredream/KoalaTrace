package com.boredream.koalatrace.utils

import android.graphics.Color
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.PolygonHoleOptions
import com.amap.api.maps.model.PolygonOptions
import com.amap.api.maps.model.PolylineOptions
import com.blankj.utilcode.util.TimeUtils
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.constant.LocationConstant
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.MultiPolygon
import org.locationtech.jts.geom.Polygon
import org.locationtech.jts.operation.buffer.BufferOp
import org.locationtech.jts.operation.buffer.BufferParameters
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
    fun isValidTrace(record: TraceRecord): String? {
        // 总轨迹点数量<xx无效
        if (record.traceList.size < LocationConstant.SAVE_TRACE_MIN_POSITION_SIZE) {
            return "轨迹点数量过少 ${record.traceList.size} < ${LocationConstant.SAVE_TRACE_MIN_POSITION_SIZE} 个"
        }
        // 总距离<xx米无效
        if (record.distance < LocationConstant.SAVE_TRACE_MIN_DISTANCE) {
            return "轨迹距离过短 ${record.distance} < ${LocationConstant.SAVE_TRACE_MIN_DISTANCE} 米"
        }
        // 总距离<可疑距离时，进一步判断起始点位置举例，如果<xx米则无效
        if (record.distance < LocationConstant.SAVE_TRACE_SUSPICIOUS_MIN_DISTANCE) {
            val distance = AMapUtils.calculateLineDistance(
                LatLng(record.traceList.first().latitude, record.traceList.first().longitude),
                LatLng(record.traceList.last().latitude, record.traceList.last().longitude)
            )
            if (distance < LocationConstant.SAVE_TRACE_MIN_DISTANCE) {
                return "轨迹起始点距离过短 $distance < ${LocationConstant.SAVE_TRACE_MIN_DISTANCE} 米"
            }
        }
        return null
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

        // 简化线路
        val lineList = arrayListOf<LineString>()
        recordList.forEach {
            lineList.add(simpleLine(it.traceList))
        }

//        // 先 line-buffer
//        val lineBufferList = arrayListOf<Geometry>()
//        lineList.forEach { lineBufferList.add(createLineBuffer(it)) }
//        // 后 merge
//        val geometryCollection = GeometryFactory().buildGeometry(lineBufferList)
//        var mergePolygon = geometryCollection
//        if(geometryCollection is GeometryCollection) {
//            mergePolygon = geometryCollection.union()
//        }

        // 先 merge line
        val mergeLine = GeometryFactory().createMultiLineString(lineList.toTypedArray())
        // 后 line-buffer
        val mergePolygon = createLineBuffer(mergeLine)

        // 多个路线拼接的图，可能合并成一个形状，也可能是分开的几个形状，需要各自单独绘制
        val mapPolygonList = arrayListOf<com.amap.api.maps.model.Polygon>()
        if (mergePolygon is Polygon) {
            mapPolygonList.addAll(drawJstPolygonMask(map, mergePolygon, color))
//            drawJstPolygon(map, mergePolygon, color)?.let { mapPolygonList.add(it) }
        } else if (mergePolygon is MultiPolygon) {
            for (i in 0 until mergePolygon.getNumGeometries()) {
                val geometry: Geometry = mergePolygon.getGeometryN(i)
                if (geometry is Polygon) {
                    mapPolygonList.addAll(drawJstPolygonMask(map, geometry, color))
//                    drawJstPolygon(map, geometry, color)?.let { mapPolygonList.add(it) }
                }
            }
        }
        return mapPolygonList
    }

    private fun simpleLine(traceList: ArrayList<TraceLocation>): LineString {
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
        // logger.i("simple line duration ${System.currentTimeMillis() - start}")
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
        // logger.i("line buffer duration ${System.currentTimeMillis() - start}")
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
        val polygonHoleOptionsList = arrayListOf<PolygonHoleOptions>()
        if (polygon.numInteriorRing > 0) {
            for (index in 0 until polygon.numInteriorRing) {
                val interRing = polygon.getInteriorRingN(index)

//                // TODO: 环如果过小，可以省略
//                val interRingPolygon = geometryFactory.createPolygon(interRing)
//                logger.i("interRingPolygon area = ${interRingPolygon.area}")

                val inter = interRing.coordinates.map { LatLng(it.x, it.y) }
                polygonHoleOptionsList.add(PolygonHoleOptions().addAll(inter))

                logger.i("add polygon hole = $index")
            }
        }
        polygonOptions.addHoles(polygonHoleOptionsList)

        logger.i("addJstPolygon duration ${System.currentTimeMillis() - start}")
        return map.addPolygon(polygonOptions)
    }

    private fun drawJstPolygonMask(
        map: AMap,
        polygon: Polygon,
        color: Int
    ): List<com.amap.api.maps.model.Polygon> {
        val start = System.currentTimeMillis()

        // 默认遮罩
        val maskPolygonOptions = PolygonOptions()
            .add(LatLng(90.0, -180.0))
            .add(LatLng(-90.0, -180.0))
            .add(LatLng(-90.0, 179.9999999999999))
            .add(LatLng(90.0, 179.9999999999999))
            .fillColor(color)
            .strokeWidth(0f)

        // 外环
        val polygonOptions = PolygonHoleOptions()
            .addAll(polygon.exteriorRing.coordinates.map { LatLng(it.x, it.y) })

        // 外环作为孔，进行基本遮罩绘制
        maskPolygonOptions.addHoles(polygonOptions)
        val list = arrayListOf<com.amap.api.maps.model.Polygon>(
            map.addPolygon(maskPolygonOptions)
        )

        // 内孔
        if (polygon.numInteriorRing > 0) {
            for (index in 0 until polygon.numInteriorRing) {
                val interRing = polygon.getInteriorRingN(index)

//                // TODO: 环如果过小，可以省略
//                val interRingPolygon = geometryFactory.createPolygon(interRing)
//                logger.i("interRingPolygon area = ${interRingPolygon.area}")

                val inter = interRing.coordinates.map { LatLng(it.x, it.y) }

                // 内孔作为遮罩内形状，单独绘制
                list.add(map.addPolygon(PolygonOptions().addAll(inter).fillColor(color).strokeWidth(0f)))

                logger.i("add polygon hole = $index")
            }
        }

        logger.i("addJstPolygon duration ${System.currentTimeMillis() - start}")
        return list
    }


}