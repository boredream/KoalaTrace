package com.boredream.koalatrace.data.repo.source

import android.content.Context
import android.os.CountDownTimer
import com.amap.api.location.AMapLocation
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeAddress
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.utils.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.random.Random

class FakeLocationDataSource @Inject constructor(
    @ApplicationContext val context: Context,
    val logger: Logger,
) : LocationDataSource {

    private lateinit var countDownTimer: CountDownTimer
    private lateinit var moveLocation: AMapLocation

    override fun startLocation(onSuccess: (location: TraceLocation) -> Unit) {
        val startLocation = AMapLocation("start")
        startLocation.latitude = 31.227792
        startLocation.longitude = 121.355379

        moveLocation = AMapLocation("move")
        moveLocation.latitude = startLocation.latitude
        moveLocation.longitude = startLocation.longitude

        // 开启定时任务，然后挨个返回虚拟经纬度
        val total = (10 * 60 * 1000 + 100).toLong()
        countDownTimer = object : CountDownTimer(total, 2000) {
            override fun onTick(millisUntilFinished: Long) = onSuccess.invoke(genStepLocation(millisUntilFinished))
            override fun onFinish() = Unit
        }
        countDownTimer.start()
    }

    override fun stopLocation() {
        countDownTimer.cancel()
    }

    fun genStepLocation(millisUntilFinished: Long): TraceLocation {
        var yStep = 0.00001 * (Random.nextInt(100) - 30)
        var xStep = 0.00001 * (Random.nextInt(50) - 20)
        val ratio = 0.3
        if(millisUntilFinished % 40000 < 15000) {
            // 走走停停
            yStep = 0.0
            xStep = 0.0
        }
        moveLocation.latitude = BigDecimal(moveLocation.latitude + yStep * ratio)
            .setScale(6, RoundingMode.HALF_UP).toDouble()
        moveLocation.longitude = BigDecimal(moveLocation.longitude + xStep * ratio)
            .setScale(6, RoundingMode.HALF_UP).toDouble()
        val location = TraceLocation(latitude = moveLocation.latitude, longitude = moveLocation.longitude)
        location.extraData = "1_3" // GPS_精度3米
        return location
    }

    override fun geocodeSearch(latitude: Double, longitude: Double, callback: ((address: RegeocodeAddress?) -> Unit)?) {
        val geocoderSearch = GeocodeSearch(context)
        geocoderSearch.setOnGeocodeSearchListener(object : GeocodeSearch.OnGeocodeSearchListener {
            override fun onRegeocodeSearched(result: RegeocodeResult?, rCode: Int) {
                if(result?.regeocodeAddress == null) {
                    callback?.invoke(null)
                    return
                }
                val address = result.regeocodeAddress
                callback?.invoke(address)
            }

            override fun onGeocodeSearched(result: GeocodeResult?, rCode: Int) {
            }

        })
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        val query = RegeocodeQuery(LatLonPoint(latitude, longitude), 200F, GeocodeSearch.AMAP)
        geocoderSearch.getFromLocationAsyn(query)
    }

}