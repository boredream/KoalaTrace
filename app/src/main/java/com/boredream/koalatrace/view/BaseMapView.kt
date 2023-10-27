package com.boredream.koalatrace.view

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.amap.api.maps.AMap.OnCameraChangeListener
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.TextureMapView
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.CustomMapStyleOptions
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.Polyline
import com.amap.api.maps.model.PolylineOptions
import com.boredream.koalatrace.R
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.utils.FileUtils
import com.boredream.koalatrace.utils.Logger


open class BaseMapView : TextureMapView {

    protected val logger = Logger()
    var zoomLevel = 17f

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        // TODO: 地图样式问题，省略一些小区名字等细节，突出线路。参考百度的一蓑烟雨 https://lbsyun.baidu.com/customv2/editor/e5dea5db69354a92addfb8e2bf7115dd/lvyexianzong

        map?.let {
            it.uiSettings.isScaleControlsEnabled = false
            it.uiSettings.isZoomControlsEnabled = false
            // 高德自定义样式 https://geohub.amap.com/mapstyle
            val styleData = FileUtils.readBytesFromAssets(context, "mapstyle/style.data")
            val styleExtraData = FileUtils.readBytesFromAssets(context, "mapstyle/style_extra.data")
            val styleOptions = CustomMapStyleOptions()
                .setEnable(true)
                .setStyleData(styleData)
                .setStyleExtraData(styleExtraData)
            it.setCustomMapStyle(styleOptions)
            it.setRoadArrowEnable(false)
            it.addOnCameraChangeListener(object : OnCameraChangeListener {
                override fun onCameraChange(position: CameraPosition?) {

                }

                override fun onCameraChangeFinish(position: CameraPosition?) {
                    position?.let { it -> zoomLevel = it.zoom }
                }
            })
        }
    }

    protected fun moveCamera(latitude: Double, longitude: Double) {
        val position = CameraPosition.Builder()
            .target(LatLng(latitude, longitude))
            .zoom(zoomLevel)
            .build()
        map.moveCamera(CameraUpdateFactory.newCameraPosition(position))
    }

    protected fun TraceLocation.toLatLng(): LatLng {
        return LatLng(this.latitude, this.longitude)
    }

}