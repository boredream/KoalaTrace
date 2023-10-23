package com.boredream.koalatrace.utils

import com.boredream.koalatrace.data.constant.LocationConstant
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryCollection
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.MultiPolygon
import org.locationtech.jts.geom.Polygon
import org.locationtech.jts.operation.buffer.BufferOp
import org.locationtech.jts.operation.buffer.BufferParameters

object JtsUtils {

    /**
     * 用多个线路，使用 Line-Buffer技术 生成合并形状
     */
    fun genMergePolygonWithLineList(lineList: ArrayList<LineString>, width: Double): ArrayList<Polygon> {
        // 先 line-buffer
        val lineBufferList = arrayListOf<Geometry>()
        lineList.forEach { lineBufferList.add(createLineBuffer(it, width)) }
        // 后 merge
        val geometryCollection = GeometryFactory().buildGeometry(lineBufferList)
        var mergePolygon = geometryCollection
        if (geometryCollection is GeometryCollection) {
            mergePolygon = geometryCollection.union()
        }

        // 这个效率更低
//        // 先 merge line
//        val mergeLine = GeometryFactory().createMultiLineString(lineList.toTypedArray())
//        // 后 line-buffer
//        val mergePolygon = createLineBuffer(mergeLine)

        return geometry2polygonList(mergePolygon)
    }

    fun createLineBuffer(line: Geometry, width: Double): Geometry {
        // line-buffer 用线计算区域面积
        val bufferParams = BufferParameters()
        bufferParams.endCapStyle = BufferParameters.CAP_ROUND
        bufferParams.joinStyle = BufferParameters.JOIN_ROUND
        val bufferOp = BufferOp(line, bufferParams)
        return bufferOp.getResultGeometry(width)
    }

    fun str2Polygon(str: String): Polygon {
        val geometryFactory = GeometryFactory()
        val coordinates = arrayListOf<Coordinate>()
        for (coordinate in str.split(";")) {
            coordinates.add(
                Coordinate(
                    coordinate.split(',')[0].toDouble(),
                    coordinate.split(',')[1].toDouble(),
                )
            )
        }
        return geometryFactory.createPolygon(coordinates.toTypedArray())
    }

    fun geometry2polygonList(geometry: Geometry): ArrayList<Polygon> {
        val list = arrayListOf<Polygon>()
        if (geometry is Polygon) {
            list.add(geometry)
        } else if (geometry is MultiPolygon) {
            for (i in 0 until geometry.getNumGeometries()) {
                val geometryItem: Geometry = geometry.getGeometryN(i)
                if (geometryItem is Polygon) {
                    list.add(geometryItem)
                }
            }
        }
        return list
    }

}