package com.boredream.koalatrace.data.repo

import com.boredream.koalatrace.PrintLogger
import com.boredream.koalatrace.TestDataConstants.getStepTraceLocation
import com.boredream.koalatrace.TestDataConstants.getTraceLocation
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.constant.LocationConstant
import com.boredream.koalatrace.data.repo.source.LocationDataSource
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocationRepositoryTest {

    @MockK
    private lateinit var dataSource: LocationDataSource

    private lateinit var repo: LocationRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repo = LocationRepository(PrintLogger(), dataSource)
    }

    @Test
    fun testLocation() = runTest {
        // 回调的mock这样处理
        every {
            dataSource.startLocation(any())
        } answers {
            firstArg<(location: TraceLocation) -> Unit>()
                .invoke(getTraceLocation())
        }
        repo.startLocation()
        assertNotNull(repo.myLocation)
    }

    @Test
    fun testTrace_notTracing() = runTest {
        // 回调的mock这样处理
        every {
            dataSource.startLocation(any())
        } answers {
            firstArg<(location: TraceLocation) -> Unit>()
                .invoke(getTraceLocation())
        }
        repo.startLocation()
        assertEquals(0, repo.traceList.size)
    }

    @Test
    fun testTrace_success() = runTest {
        every {
            dataSource.startLocation(any())
        } just runs

        repo.startLocation()
        repo.startTrace()
        repo.onLocationSuccess(getStepTraceLocation())
        repo.onLocationSuccess(getStepTraceLocation())
        assertEquals(2, repo.traceList.size)
    }

    @Test
    fun testTrace_stepTooNear() = runTest {
        every {
            dataSource.startLocation(any())
        } just runs

        repo.startLocation()
        repo.startTrace()
        repo.onLocationSuccess(
            TraceLocation(
                latitude = 31.000000 + LocationConstant.ONE_METER_LAT_LNG * 20,
                longitude = 121.000000,
                time = 0,
            )
        )
        repo.onLocationSuccess(
            TraceLocation(
                latitude = 31.000000,
                longitude = 121.000000,
                time = 5000,
            )
        )
        repo.onLocationSuccess(
            TraceLocation(
                latitude = 31.000000,
                longitude = 121.000000,
                time = 10000,
            )
        )
        repo.onLocationSuccess(
            TraceLocation(
                latitude = 31.000000,
                longitude = 121.000000,
                time = 15000,
            )
        )
        assertEquals(2, repo.traceList.size)

        val lastIndex = repo.traceList.lastIndex
        val timeDiff = repo.traceList[lastIndex].time - repo.traceList[lastIndex - 1].time
        println("timeDiff = $timeDiff")
        assertTrue(timeDiff > LocationConstant.LOCATION_INTERVAL * 2 - 500)
    }

    @Test
    fun testTrace_stepTooFar() = runTest {
        every {
            dataSource.startLocation(any())
        } just runs

        repo.startLocation()
        repo.startTrace()
        repo.onLocationSuccess(
            TraceLocation(
                latitude = 31.000000,
                longitude = 121.000000,
                time = 0,
            )
        )
        repo.onLocationSuccess(
            TraceLocation(
                latitude = 31.000000 + LocationConstant.ONE_METER_LAT_LNG * 20,
                longitude = 121.000000,
                time = 5000,
            )
        )
        repo.onLocationSuccess(
            TraceLocation(
                latitude = 31.000000 + LocationConstant.ONE_METER_LAT_LNG * 1000,
                longitude = 121.000000,
                time = 10000,
            )
        )
        repo.onLocationSuccess(
            TraceLocation(
                latitude = 31.000000 + LocationConstant.ONE_METER_LAT_LNG * 2000,
                longitude = 121.000000,
                time = 15000,
            )
        )
        assertEquals(2, repo.traceList.size)
    }

    @Test
    fun testTrace_clear() = runTest {
        // 因为 dataSource 定位是定时持续返回的，而非每次调用 startLocation 返回
        // 所以多段多次回调，不用 answers arg 方式，直接手动 onSuccess 触发回调
        every {
            dataSource.startLocation(any())
        } just runs

        repo.startLocation()
        repo.startTrace()
        repo.onLocationSuccess(getStepTraceLocation())
        repo.onLocationSuccess(getStepTraceLocation())

        assertEquals(2, repo.traceList.size)

        // 停止记录轨迹
        repo.stopTrace()
        repo.onLocationSuccess(getStepTraceLocation())
        assertEquals(2, repo.traceList.size)

        // 清理轨迹
        repo.clearTraceList()
        assertEquals(0, repo.traceList.size)

        // 再次开启轨迹记录
        repo.startTrace()
        repo.onLocationSuccess(getStepTraceLocation())
        repo.onLocationSuccess(getStepTraceLocation())
        repo.onLocationSuccess(getStepTraceLocation())
        assertEquals(3, repo.traceList.size)

    }

}