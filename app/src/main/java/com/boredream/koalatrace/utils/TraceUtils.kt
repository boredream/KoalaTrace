package com.boredream.koalatrace.utils

import com.amap.api.maps.AMap
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.PolygonHoleOptions
import com.amap.api.maps.model.PolygonOptions
import com.blankj.utilcode.util.TimeUtils
import com.boredream.koalatrace.data.ExploreBlockInfo
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.constant.LocationConstant
import com.boredream.koalatrace.data.constant.MapConstant
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryCollection
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.MultiPolygon
import org.locationtech.jts.geom.Polygon
import org.locationtech.jts.geom.Polygonal
import org.locationtech.jts.operation.buffer.BufferOp
import org.locationtech.jts.operation.buffer.BufferParameters
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier
import kotlin.math.cos
import kotlin.math.pow


object TraceUtils {

    private val logger = PrintLogger()
    private val geometryFactory = GeometryFactory()

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

        // 先 line-buffer
        val lineBufferList = arrayListOf<Geometry>()
        lineList.forEach { lineBufferList.add(createLineBuffer(it)) }
        // 后 merge
        val geometryCollection = GeometryFactory().buildGeometry(lineBufferList)
        var mergePolygon = geometryCollection
        if (geometryCollection is GeometryCollection) {
            mergePolygon = geometryCollection.union()
        }

//        // 先 merge line
//        val mergeLine = GeometryFactory().createMultiLineString(lineList.toTypedArray())
//        // 后 line-buffer
//        val mergePolygon = createLineBuffer(mergeLine)

        // 多个路线拼接的图，可能合并成一个形状，也可能是分开的几个形状，需要各自单独绘制
        val polygonList = arrayListOf<Polygon>()
        if (mergePolygon is Polygon) {
            polygonList.add(mergePolygon)
        } else if (mergePolygon is MultiPolygon) {
            for (i in 0 until mergePolygon.getNumGeometries()) {
                val geometry: Geometry = mergePolygon.getGeometryN(i)
                if (geometry is Polygon) {
                    polygonList.add(geometry)
                }
            }
        }
        return drawJstPolygonMask(map, polygonList, color)
    }

    fun simpleLine(traceList: ArrayList<TraceLocation>): LineString {
        // TODO: https://lbs.amap.com/demo/sdk/path-smooth#android 轨迹平滑处理
        // 先经纬度转为jts的line对象
        val factory = GeometryFactory()
        val coordinateList = arrayListOf<Coordinate>()
        traceList.forEach { coordinateList.add(Coordinate(it.latitude, it.longitude)) }
        val line = factory.createLineString(coordinateList.toTypedArray())

        // 简化线的几何形状
        val tolerance = LocationConstant.ONE_METER_LAT_LNG * 20 // 简化容差
        val simplifier = DouglasPeuckerSimplifier(line)
        simplifier.setDistanceTolerance(tolerance)
        return simplifier.resultGeometry as LineString
    }

    fun createLineBuffer(line: Geometry): Geometry {
        // line-buffer 用线计算区域面积
        val bufferParams = BufferParameters()
        bufferParams.endCapStyle = BufferParameters.CAP_ROUND
        bufferParams.joinStyle = BufferParameters.JOIN_ROUND
        val bufferOp = BufferOp(line, bufferParams)
        val width = LocationConstant.ONE_METER_LAT_LNG * 50
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

                val inter = interRing.coordinates.map { LatLng(it.x, it.y) }
                polygonHoleOptionsList.add(PolygonHoleOptions().addAll(inter))

                logger.v("add polygon hole = $index")
            }
        }
        polygonOptions.addHoles(polygonHoleOptionsList)

        logger.i("addJstPolygon duration ${System.currentTimeMillis() - start}")
        return map.addPolygon(polygonOptions)
    }

    private fun drawJstPolygonMask(
        map: AMap,
        polygonList: ArrayList<Polygon>,
        color: Int
    ): List<com.amap.api.maps.model.Polygon> {
        val start = System.currentTimeMillis()

        val list = arrayListOf<com.amap.api.maps.model.Polygon>()

        // 默认遮罩
        val maskPolygonOptions = PolygonOptions()
            .add(LatLng(90.0, -180.0))
            .add(LatLng(-90.0, -180.0))
            .add(LatLng(-90.0, 179.9999999999999))
            .add(LatLng(90.0, 179.9999999999999))
            .fillColor(color)
            .strokeWidth(0f)

        // 外环作为孔，进行基本遮罩绘制
        polygonList.forEach { polygon ->
            val coordinates = polygon.exteriorRing.coordinates.map { LatLng(it.x, it.y) }
            maskPolygonOptions.addHoles(PolygonHoleOptions().addAll(coordinates))
        }
        list.add(map.addPolygon(maskPolygonOptions))

        // 内孔
        polygonList.filter { it.numInteriorRing > 0 }
            .forEach { polygon ->
                for (index in 0 until polygon.numInteriorRing) {
                    val interRing = polygon.getInteriorRingN(index)

                    // 环如果过小，可以省略
                    val interRingPolygon = geometryFactory.createPolygon(interRing)
                    if (interRingPolygon.area < MapConstant.IGNORE_INTER_RING_AREA) {
                        continue
                    }

                    // 内孔作为遮罩内形状，单独绘制
                    val polygonOptions = PolygonOptions()
                        .addAll(interRing.coordinates.map { LatLng(it.x, it.y) })
                        .fillColor(color)
                        .strokeWidth(0f)
                    list.add(map.addPolygon(polygonOptions))
                    logger.v("add polygon hole = $index , area = ${interRingPolygon.area}")
                }
            }

        list.forEach { it.zIndex = 99999f }
        logger.i("addJstPolygon duration ${System.currentTimeMillis() - start}")
        return list
    }

    /**
     * 把区域按方格分割
     */
    fun splitCityDistinct(areaCode: String, boundary: ArrayList<LatLng>): ArrayList<ExploreBlockInfo> {
        // Pair<方形外框，实际形状>
        val splitRectList = arrayListOf<ExploreBlockInfo>()

        // 先计算边界
        var left: Double = Double.MAX_VALUE
        var bottom: Double = Double.MAX_VALUE
        var right: Double = Double.MIN_VALUE
        var top: Double = Double.MIN_VALUE
        boundary.forEach {
            left = left.coerceAtMost(it.longitude)
            bottom = bottom.coerceAtMost(it.latitude)
            right = right.coerceAtLeast(it.longitude)
            top = top.coerceAtLeast(it.latitude)
        }

        // 计算分割的正方形边长，经纬度的数字和米的对应关系不同，需要处理
        // 维度比较固定，1度约等于111公里
        val deltaLatitude = 1.0 / 111000
        // 经度需要根据当前维度，进行计算
        val deltaLongitude = 1.0 / (cos(Math.toRadians(bottom)) * 111000)
        // 计算 AREA_SPLIT_SQUARE_LENGTH 米 需要的实际经纬度数字
        val squareHeight = deltaLatitude * MapConstant.AREA_SPLIT_SQUARE_LENGTH
        val squareWidth = deltaLongitude * MapConstant.AREA_SPLIT_SQUARE_LENGTH

        // 计算方形阵列的起点，要刚刚好包裹住边界，且尽量居中显示
        val totalWidth = right - left
        val xCount = (totalWidth / squareWidth).toInt()
        val startLongitudeOffset = ((xCount + 1) * squareWidth - totalWidth) / 2
        val totalHeight = top - bottom
        val yCount = (totalHeight / squareHeight).toInt()
        val startLatitudeOffset = ((yCount + 1) * squareHeight - totalHeight) / 2

        // 从左下角开始，生成阵列
        val startLocation = LatLng(bottom - startLatitudeOffset, left - startLongitudeOffset)
        val geometryFactory = GeometryFactory()
        // 总的边界形状，用于和每个正方形的交集计算
        val boundaryPolygon = geometryFactory.createPolygon(
            boundary.map { Coordinate(it.longitude, it.latitude) }.toTypedArray()
        )
        for (x in 0..xCount) {
            for (y in 0..yCount) {
                val iStartLoc = LatLng(
                    startLocation.latitude + y * squareHeight,
                    startLocation.longitude + x * squareWidth
                )

                // 计算边界形状和每个正方形，无交集，或者交集过小都忽略
                val rectPolygon = geometryFactory.createPolygon(arrayOf(
                    Coordinate(iStartLoc.longitude, iStartLoc.latitude),
                    Coordinate(iStartLoc.longitude, iStartLoc.latitude + squareHeight),
                    Coordinate(iStartLoc.longitude + squareWidth, iStartLoc.latitude + squareHeight),
                    Coordinate(iStartLoc.longitude + squareWidth, iStartLoc.latitude),
                    Coordinate(iStartLoc.longitude, iStartLoc.latitude),
                ))
                val intersection = boundaryPolygon.intersection(rectPolygon)
                if (intersection.isEmpty) {
                    continue
                }
                val areaRatio = intersection.area / rectPolygon.area
                if (areaRatio < MapConstant.AREA_SPLIT_IGNORE_AREA_RATIO) {
                    continue
                }

                val sbRect = StringBuffer()
                rectPolygon.coordinates.forEach {
                    sbRect.append(";").append(it.y).append(",").append(it.x)
                }

                val sbActual = StringBuilder()
                if (intersection is Polygon) {
                    intersection.coordinates.forEach {
                        sbActual.append(";").append(it.y).append(",").append(it.x)
                    }
                } else if (intersection is MultiPolygon) {
                    for (i in 0 until intersection.getNumGeometries()) {
                        val geometry: Geometry = intersection.getGeometryN(i)
                        if (geometry is Polygon) {
                            if (i > 0) sbActual.append("==")
                            geometry.coordinates.forEach {
                                sbActual.append(";").append(it.y).append(",").append(it.x)
                            }
                        }
                    }
                }
                splitRectList.add(
                    ExploreBlockInfo(
                        areaCode,
                        sbRect.substring(1),
                        sbActual.substring(1),
                        intersection.area
                    )
                )
            }
        }
        return splitRectList
    }


}