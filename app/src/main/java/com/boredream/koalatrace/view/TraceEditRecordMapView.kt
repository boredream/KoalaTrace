package com.boredream.koalatrace.view

import android.content.Context
import android.util.AttributeSet
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.boredream.koalatrace.data.TraceLocation


class TraceEditRecordMapView : RecordMapView {

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

    fun updateSelect(location: TraceLocation) {
        marker?.remove()
        val latLng = LatLng(location.latitude, location.longitude)
        marker = map.addMarker(MarkerOptions().position(latLng))
        moveCamera(location)
    }

}