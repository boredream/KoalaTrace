package com.boredream.koalatrace

import com.amap.api.maps.model.LatLng
import com.boredream.koalatrace.data.constant.MapConstant
import io.mockk.MockKAnnotations
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.math.cos

@OptIn(ExperimentalCoroutinesApi::class)
class TraceUtilsTest {

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun testDelta() = runTest {
        val latitude = 36.0
        val deltaLatitude = 1.0 / 111000 // 1度约等于111公里
        val deltaLongitude = 1.0 / (cos(Math.toRadians(latitude)) * 111000)
        println("deltaLatitude=$deltaLatitude , deltaLongitude=$deltaLongitude")
    }

    @Test
    fun test() = runTest {

    }

}