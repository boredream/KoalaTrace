package com.boredream.koalatrace.view

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.amap.api.maps.AMap
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.Polyline
import com.boredream.koalatrace.R
import com.boredream.koalatrace.data.TraceLocation


/**
 * 追踪路线的地图
 */
class TraceEditMapView : BaseTraceMapView {

    private var line: Polyline? = null
    private var marker: Marker? = null
    var onMapClickListener: (latLng: LatLng) -> Unit = {}

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        map.setOnMapClickListener { onMapClickListener.invoke(it) }
    }

    fun clearLine() {
        line?.remove()
    }

    override fun drawTraceList(
        traceList: ArrayList<TraceLocation>,
        traceLineWidth: Float,
        traceLineColor: Int
    ): Polyline? {
        line = super.drawTraceList(traceList, traceLineWidth, traceLineColor)
        return line
    }

    fun deleteLinePoint(position: Int) {
        line?.let {
            val newPoints = it.points.toMutableList()
            newPoints.removeAt(position)
            it.points = newPoints
            invalidate()
            logger.i("deleteLinePoint , index = $position")
        }
    }

    fun updateSelect(location: TraceLocation) {
        marker?.remove()
        val latLng = LatLng(location.latitude, location.longitude)
        marker = map.addMarker(MarkerOptions().position(latLng))
        moveCamera(location)
    }

}