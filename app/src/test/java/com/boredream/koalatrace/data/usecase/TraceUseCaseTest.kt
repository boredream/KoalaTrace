package com.boredream.koalatrace.data.usecase

import com.boredream.koalatrace.PrintLogger
import com.boredream.koalatrace.TestDataConstants
import com.boredream.koalatrace.data.ResponseEntity
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.repo.LocationRepository
import com.boredream.koalatrace.data.repo.TraceRecordRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TraceUseCaseTest {

    @MockK
    private lateinit var locationRepository: LocationRepository

    @MockK
    private lateinit var traceRecordRepository: TraceRecordRepository

    private lateinit var useCase: TraceUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = TraceUseCase(PrintLogger(), locationRepository, traceRecordRepository)

        // use case 是连接多个 repo 的逻辑处理类，不需要在意repo细节
        every { locationRepository.startLocation() } just runs
        every { locationRepository.clearTraceList() } just runs
        every { locationRepository.startTrace() } just runs
        every { locationRepository.stopTrace() } just runs
        coEvery { runBlocking { traceRecordRepository.insertOrUpdate(any()) } } returns
                ResponseEntity.success(TraceRecord("", 0, 0, 0))
        coEvery { runBlocking { traceRecordRepository.updateByTraceList(any()) } } just runs
        coEvery { runBlocking { traceRecordRepository.insertOrUpdateLocation(any(), any()) } } returns
                ResponseEntity.success(TraceLocation(0.0, 0.0))
    }

    @Test
    fun testTraceNormal() = runTest {
        every { locationRepository.status } returns LocationRepository.STATUS_IDLE
        useCase.startLocation()
        every { locationRepository.status } returns LocationRepository.STATUS_LOCATION
        runBlocking { useCase.startTrace() }
        every { locationRepository.status } returns LocationRepository.STATUS_TRACE

        // 开始定位就创建轨迹
        verify(exactly = 1) { locationRepository.startLocation() }
        verify(exactly = 1) { locationRepository.startTrace() }
        verify(exactly = 1) { runBlocking { traceRecordRepository.insertOrUpdate(any()) } }
        Assert.assertNotNull(useCase.currentTraceRecord)

        // 定位了部分数据
        every { locationRepository.traceList } returns TestDataConstants.getTraceList()

        // 插入轨迹点
        val locationCount = 5
        for (i in 0 until locationCount) {
            useCase.addLocation2currentRecord()
        }
        verify(exactly = 5) { runBlocking { traceRecordRepository.insertOrUpdateLocation(any(), any()) } }

        // 停止并更新记录
        runBlocking { useCase.stopTrace() }
        verify(exactly = 1) { runBlocking { locationRepository.stopTrace() } }
        verify(exactly = 1) { runBlocking { traceRecordRepository.updateByTraceList(any()) } }
        verify(exactly = 1) { runBlocking { locationRepository.clearTraceList() } }
    }

    @Test
    fun testAutoStop() = runTest {
        every { locationRepository.status } returns LocationRepository.STATUS_IDLE
        useCase.startLocation()
        every { locationRepository.status } returns LocationRepository.STATUS_LOCATION
        runBlocking { useCase.startTrace() }
        every { locationRepository.status } returns LocationRepository.STATUS_TRACE

        val locationList = TestDataConstants.getTraceList()
        locationList.add(locationList.last())

        // 定位了部分数据
        every { locationRepository.traceList } returns TestDataConstants.getTraceList()

        // 插入轨迹点
        useCase.addLocation2currentRecord()
        verify(exactly = 1) { runBlocking { traceRecordRepository.insertOrUpdateLocation(any(), any()) } }

        // 停止并更新记录
        runBlocking { useCase.stopTrace() }
        verify(exactly = 1) { runBlocking { locationRepository.stopTrace() } }
        verify(exactly = 1) { runBlocking { traceRecordRepository.updateByTraceList(any()) } }
        verify(exactly = 1) { runBlocking { locationRepository.clearTraceList() } }
    }

}