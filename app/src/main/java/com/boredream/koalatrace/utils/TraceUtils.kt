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
import com.boredream.koalatrace.utils.JtsUtils.genMergePolygonWithLineList
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.MultiPolygon
import org.locationtech.jts.geom.Polygon
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

    /**
     * 根据轨迹线路列表生成探索区域形状列表
     */
    fun genExplorePolygon(recordList: List<TraceRecord>): ArrayList<Polygon> {
        // 简化线路
        val lineList = arrayListOf<LineString>()
        recordList.forEach {
            lineList.add(simpleLine(it.traceList))
        }

        val lineBufferWidth = LocationConstant.ONE_METER_LAT_LNG * 50
        return genMergePolygonWithLineList(lineList, lineBufferWidth)
    }

    fun simpleLine(traceList: ArrayList<TraceLocation>): LineString {
        // TODO: only public for test
        // TODO: https://lbs.amap.com/demo/sdk/path-smooth#android 轨迹平滑处理
        // 先经纬度转为jts的line对象
        val factory = GeometryFactory()
        val coordinateList = arrayListOf<Coordinate>()
        traceList.forEach { coordinateList.add(Coordinate(it.longitude, it.latitude)) }
        val line = factory.createLineString(coordinateList.toTypedArray())

        // 简化线的几何形状
        val tolerance = LocationConstant.ONE_METER_LAT_LNG * 20 // 简化容差
        val simplifier = DouglasPeuckerSimplifier(line)
        simplifier.setDistanceTolerance(tolerance)
        return simplifier.resultGeometry as LineString
    }

    private fun drawJstPolygon(
        map: AMap,
        polygon: Polygon,
        color: Int
    ): com.amap.api.maps.model.Polygon? {
        val start = System.currentTimeMillis()

        // 外环
        val polygonOptions = PolygonOptions()
            .addAll(coord2LatLngList(polygon.exteriorRing.coordinates))
            .fillColor(color)
            .strokeWidth(0f)

        // 内孔
        val polygonHoleOptionsList = arrayListOf<PolygonHoleOptions>()
        if (polygon.numInteriorRing > 0) {
            for (index in 0 until polygon.numInteriorRing) {
                val interRing = polygon.getInteriorRingN(index)

                val inter = coord2LatLngList(interRing.coordinates)
                polygonHoleOptionsList.add(PolygonHoleOptions().addAll(inter))

                logger.v("add polygon hole = $index")
            }
        }
        polygonOptions.addHoles(polygonHoleOptionsList)

        logger.i("addJstPolygon duration ${System.currentTimeMillis() - start}")
        return map.addPolygon(polygonOptions)
    }

    fun drawJstPolygonMask(
        map: AMap,
        maskLatLngList: ArrayList<LatLng> = arrayListOf(
            LatLng(90.0, -180.0),
            LatLng(-90.0, -180.0),
            LatLng(-90.0, 179.9999999999999),
            LatLng(90.0, 179.9999999999999),
        ),
        polygonList: ArrayList<Polygon>,
        color: Int
    ): List<com.amap.api.maps.model.Polygon> {
        val start = System.currentTimeMillis()

        val list = arrayListOf<com.amap.api.maps.model.Polygon>()

        // 默认遮罩
        val maskPolygonOptions = PolygonOptions()
            .addAll(maskLatLngList)
            .fillColor(color)
            .strokeWidth(0f)

        // 外环作为孔，进行基本遮罩绘制
        polygonList.forEach { polygon ->
            val coordinates = coord2LatLngList(polygon.exteriorRing.coordinates)
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
                        .addAll(coord2LatLngList(interRing.coordinates))
                        .fillColor(color)
                        .strokeWidth(0f)
                    list.add(map.addPolygon(polygonOptions))
                    logger.v("add polygon hole = $index , area = ${interRingPolygon.area}")
                }
            }

        list.forEach { it.zIndex = MapConstant.FROG_MAP_Z_INDEX }
        logger.i("addJstPolygon duration ${System.currentTimeMillis() - start}")
        return list
    }

    /**
     * 把区域按方格分割
     */
    fun splitDistinctToBlockList(
        areaCode: String,
        boundary: ArrayList<LatLng>
    ): ArrayList<ExploreBlockInfo> {
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
        val coordinates = latLng2coordrList(boundary).toTypedArray()
        val boundaryPolygon = geometryFactory.createPolygon(coordinates)
        for (x in 0..xCount) {
            for (y in 0..yCount) {
                val iStartLoc = LatLng(
                    startLocation.latitude + y * squareHeight,
                    startLocation.longitude + x * squareWidth
                )

                // 计算边界形状和每个正方形，无交集，或者交集过小都忽略
                val rectPolygon = geometryFactory.createPolygon(
                    arrayOf(
                        Coordinate(iStartLoc.longitude, iStartLoc.latitude),
                        Coordinate(iStartLoc.longitude, iStartLoc.latitude + squareHeight),
                        Coordinate(
                            iStartLoc.longitude + squareWidth,
                            iStartLoc.latitude + squareHeight
                        ),
                        Coordinate(iStartLoc.longitude + squareWidth, iStartLoc.latitude),
                        Coordinate(iStartLoc.longitude, iStartLoc.latitude),
                    )
                )
                val intersection = boundaryPolygon.intersection(rectPolygon)
                if (intersection.isEmpty) {
                    continue
                }
                val areaRatio = intersection.area / rectPolygon.area
                if (areaRatio < MapConstant.AREA_SPLIT_IGNORE_AREA_RATIO) {
                    continue
                }

                val rectStr = coordinate2str(rectPolygon.coordinates)

                var actualStr = ""
                if (intersection is Polygon) {
                    actualStr = coordinate2str(intersection.coordinates)
                } else if (intersection is MultiPolygon) {
                    for (i in 0 until intersection.getNumGeometries()) {
                        val geometry: Geometry = intersection.getGeometryN(i)
                        if (geometry is Polygon) {
                            if (i > 0) actualStr += "=="
                            actualStr += coordinate2str(geometry.coordinates)
                        }
                    }
                }
                splitRectList.add(ExploreBlockInfo(areaCode, rectStr, actualStr, intersection.area))
            }
        }
        return splitRectList
    }

    // 经纬度转换
    fun latLng2coordrList(latLngList: ArrayList<LatLng>): List<Coordinate> {
        return latLngList.map { Coordinate(it.longitude, it.latitude) }
    }

    fun coord2LatLngList(coordinates: Array<Coordinate>): List<LatLng> {
        return coordinates.map { LatLng(it.y, it.x) }
    }

    fun str2LatLngList(str: String): ArrayList<LatLng> {
        // 高德 districtBoundary 的格式为 121.41257,31.191118;121.412083,31.191139; ...
        val boundary = arrayListOf<LatLng>()
        for (coordinate in str.split(";")) {
            boundary.add(
                LatLng(
                    coordinate.split(',')[1].toDouble(),
                    coordinate.split(',')[0].toDouble()
                )
            )
        }
        return boundary
    }

    private fun coordinate2str(coordinates: Array<Coordinate>): String {
        val sbRect = StringBuffer()
        coordinates.forEach {
            sbRect.append(";").append(it.x).append(",").append(it.y)
        }
        return sbRect.substring(1)
    }
}