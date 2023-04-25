package com.boredream.koalatrace.data.usecase

import com.boredream.koalatrace.PrintLogger
import com.boredream.koalatrace.TestDataConstants
import com.boredream.koalatrace.data.ResponseEntity
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.constant.LocationConstant
import com.boredream.koalatrace.data.repo.LocationRepository
import com.boredream.koalatrace.data.repo.SensorRepository
import com.boredream.koalatrace.data.repo.TraceRecordRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TraceUseCaseTest {

    @MockK(relaxed = true)
    private lateinit var locationRepository: LocationRepository

    @MockK(relaxed = true)
    private lateinit var traceRecordRepository: TraceRecordRepository

    @MockK(relaxed = true)
    private lateinit var sensorRepository: SensorRepository

    private lateinit var useCase: TraceUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        useCase = TraceUseCase(PrintLogger(), locationRepository, traceRecordRepository, sensorRepository, scope)

        // use case 是连接多个 repo 的逻辑处理类，不需要在意repo细节
        coEvery { runBlocking { traceRecordRepository.insertOrUpdate(any()) } } returns
                ResponseEntity.success(TraceRecord("", 0, 0, 0))
        coEvery { runBlocking { traceRecordRepository.insertOrUpdateLocation(any(), any()) } } returns
                ResponseEntity.success(TraceLocation(0.0, 0.0))
    }

    private fun startTrace() = runBlocking {
        useCase.startLocation()
        every { locationRepository.status } returns LocationRepository.STATUS_LOCATION
        useCase.startTrace()
        every { locationRepository.status } returns LocationRepository.STATUS_TRACE

        // 开始定位就创建轨迹
        verify(exactly = 1) { locationRepository.startLocation() }
        verify(exactly = 1) { locationRepository.startTrace() }
        verify(exactly = 1) { runBlocking { traceRecordRepository.insertOrUpdate(any()) } }
        Assert.assertNotNull(useCase.currentTraceRecord)
    }

    @Test
    fun testTraceNormal() = runTest {
        startTrace()

        val locationCount = 5
        val locationList = arrayListOf<TraceLocation>()
        repeat(locationCount) {
            locationList.add(TestDataConstants.getStepTraceLocation())
            useCase.onTraceSuccess(locationList)
        }

        verify(exactly = locationCount) { runBlocking { traceRecordRepository.insertOrUpdateLocation(any(), any()) } }
        Assert.assertEquals(locationCount, useCase.currentTraceRecord?.traceList?.size)

        // 停止并更新记录
        runBlocking { useCase.stopTrace() }
        verify(exactly = 1) { runBlocking { locationRepository.stopTrace() } }
        verify(exactly = 1) { runBlocking { traceRecordRepository.updateByTraceList(any()) } }
        verify(exactly = 1) { runBlocking { locationRepository.clearTraceList() } }
        Assert.assertNull(useCase.currentTraceRecord)
    }

    @Test
    fun testAutoStop() = runTest {
        startTrace()

        // 先正常定位
        val locationList = arrayListOf<TraceLocation>()
        repeat(5) {
            locationList.add(TestDataConstants.getStepTraceLocation())
            useCase.onTraceSuccess(locationList)
        }
        verify(exactly = 0) { locationRepository.stopTrace() }
        verify(exactly = 0) { sensorRepository.startListenerMovement() }

        // 然后停留在一个地方
        val location = TraceLocation(locationList.last().latitude, locationList.last().longitude,
            locationList.last().time + LocationConstant.STOP_THRESHOLD_DURATION + 500)
        locationList.add(location)
        useCase.onTraceSuccess(locationList)

        verify(exactly = 1) { locationRepository.stopTrace() }
        verify(exactly = 1) { sensorRepository.startListenerMovement() }
    }

    @Test
    fun testMovementAgain() = runTest {
        // FIXME: 这个方法本来是想测试传感器超过阈值后，再次恢复的情况。但这个应该是放在下一级 SensorRepository 中测试，而非 useCase。
    }

}