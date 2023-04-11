package com.boredream.koalatrace.data.repo.source

import android.content.Context
import android.util.Log
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.constant.LocationConstant
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GdLocationDataSource @Inject constructor(@ApplicationContext val context: Context) :
    LocationDataSource {

    companion object {
        const val TAG = "GdLocationDataSource"
    }

    private var locationClient: AMapLocationClient? = null

    override fun startLocation(onSuccess: (location: TraceLocation) -> Unit) {
        // TODO: on error
        try {
            locationClient = AMapLocationClient(context)
            locationClient?.let {
                initClient(it, onSuccess)
                it.startLocation()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception:$e")
        }
    }

    private fun initClient(
        client: AMapLocationClient,
        onSuccess: (location: TraceLocation) -> Unit
    ) {
        // 初始化定位参数
        val locationOption = AMapLocationClientOption()
        // 设置定位监听
        client.setLocationListener {
            if (it.errorCode == 0) {
                val location = TraceLocation(
                    latitude = it.latitude,
                    longitude = it.longitude,
                )
                // TODO: 记录除了经纬度以外的其他重要信息
                location.extraData =  "locationType：${it.locationType}, " + // 定位类型 https://lbs.amap.com/api/android-location-sdk/guide/utilities/location-type/
                        "accuracy：${it.accuracy}, " + // 精度 米
                        "speed：${it.speed}, " + // 速度 米/秒
                        "bearing：${it.bearing}, " // 角度 0~360
                onSuccess.invoke(location)
            } else Log.e(
                TAG, "initClient Error, ErrCode:" + it.errorCode + ", errInfo:" + it.errorInfo
            )
        }

        // 配置文档 http://a.amap.com/lbs/static/unzip/Android_Location_Doc/index.html?com/amap/api/location/class-use/AMapLocationClientOption.html

        // 设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        locationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        // 设置定位间隔,单位毫秒,默认为2000ms
        locationOption.interval = LocationConstant.locationInterval
        // 定位同时是否需要返回地址描述。
        locationOption.isNeedAddress = false
        // 4g网络定位精度是500~2000，wifi是5~200。从精度上4g没必要，从场景上wifi没必要。所以只用 Device_Sensors
        // 仅用设备定位模式：不需要连接网络，只使用GPS进行定位，这种模式下不支持室内环境的定位，需要在室外环境下才可以成功定位。
        // locationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Device_Sensors

        // 设置定位参数
        client.setLocationOption(locationOption)
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        // 启动定位
    }

    override fun stopLocation() {
        locationClient?.stopLocation()
    }

}