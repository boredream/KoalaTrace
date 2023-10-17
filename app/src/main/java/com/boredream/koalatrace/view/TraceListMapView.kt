package com.boredream.koalatrace.view

import android.content.Context
import android.graphics.Color
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
import com.boredream.koalatrace.utils.TraceUtils


open class TraceListMapView : RecordMapView {

    private var traceList: ArrayList<TraceRecord>? = null
    private var lineBufferPolygon: List<Polygon>? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun clearLineList() {
        super.clearLineList()
        traceList = null
    }

    /**
     * 绘制多条不会变化的线路
     */
    override fun drawMultiFixTraceList(
        traceList: ArrayList<TraceRecord>,
        traceLineWidth: Float,
        traceLineColor: Int
    ) {
        super.drawMultiFixTraceList(traceList, traceLineWidth, traceLineColor)
        this.traceList = traceList
    }

    fun drawLineBuffer(color: Int = Color.argb(100, 0, 0, 0)) {
        if(traceList == null) return
        lineBufferPolygon?.forEach { it.remove() }
        lineBufferPolygon = TraceUtils.drawTraceListLineBuffer(map, traceList!!, color)
    }

}