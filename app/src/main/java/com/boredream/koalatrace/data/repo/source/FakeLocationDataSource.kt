package com.boredream.koalatrace.data.repo.source

import android.os.CountDownTimer
import com.amap.api.location.AMapLocation
import com.boredream.koalatrace.utils.TraceFilter
import com.boredream.koalatrace.data.TraceLocation
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.random.Random

class FakeLocationDataSource @Inject constructor() : LocationDataSource {

    private lateinit var traceFilter: TraceFilter
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var moveLocation: AMapLocation

    override fun startLocation(onSuccess: (location: TraceLocation) -> Unit) {
        val startLocation = AMapLocation("start")
        startLocation.latitude = 31.227792
        startLocation.longitude = 121.355379

        moveLocation = AMapLocation("move")
        moveLocation.latitude = startLocation.latitude
        moveLocation.longitude = startLocation.longitude

        traceFilter = TraceFilter()

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

}