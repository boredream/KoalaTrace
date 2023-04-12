package com.boredream.koalatrace.data.repo.source

import android.os.CountDownTimer
import com.amap.api.location.AMapLocation
import com.boredream.koalatrace.utils.TraceFilter
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.db.AppDatabase
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
            override fun onTick(millisUntilFinished: Long) = onSuccess.invoke(testStepLocation())
            override fun onFinish() = Unit
        }
        countDownTimer.start()
    }

    override fun stopLocation() {
        countDownTimer.cancel()
    }

    fun testStepLocation(): TraceLocation {
        val yStep = 0.00001 * (Random.nextInt(100) - 30)
        val xStep = 0.00001 * (Random.nextInt(50) - 20)
        val ratio = 0.3
        moveLocation.latitude = moveLocation.latitude + yStep * ratio
        moveLocation.longitude = moveLocation.longitude + xStep * ratio
        return TraceLocation(latitude = moveLocation.latitude, longitude = moveLocation.longitude)
    }

}