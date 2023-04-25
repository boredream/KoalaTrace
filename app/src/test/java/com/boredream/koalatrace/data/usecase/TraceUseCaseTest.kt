package com.boredream.koalatrace.data.usecase

import com.boredream.koalatrace.PrintLogger
import com.boredream.koalatrace.TestDataConstants
import com.boredream.koalatrace.TestDataConstants.user
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
import java.util.*
import kotlin.collections.ArrayList

@OptIn(ExperimentalCoroutinesApi::class)
class TraceUseCaseTest {

    @MockK(relaxed = true)
    private lateinit var locationRepository: LocationRepository

    @MockK(relaxed = true)
    private lateinit var traceRecordRepository: TraceRecordRepository

    private lateinit var useCase: TraceUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = TraceUseCase(PrintLogger(), locationRepository, traceRecordRepository)

        // use case 是连接多个 repo 的逻辑处理类，不需要在意repo细节
        coEvery { runBlocking { traceRecordRepository.insertOrUpdate(any()) } } returns
                ResponseEntity.success(TraceRecord("", 0, 0, 0))
        coEvery { runBlocking { traceRecordRepository.insertOrUpdateLocation(any(), any()) } } returns
                ResponseEntity.success(TraceLocation(0.0, 0.0))
    }

    private fun startTrace() = runBlocking {
        useCase.startLocation()
        every { locationRepository.status } returns LocationRepository.STATUS_LOCATION
        runBlocking { useCase.startTrace() }
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

        // 插入轨迹点
        val locationCount = 5
        val locationList = arrayListOf<TraceLocation>()
        for (i in 0 until locationCount) {
            locationList.add(TestDataConstants.getStepTraceLocation())
            useCase.addLocation2currentRecord(locationList)
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

        // 定位了部分数据
        every { locationRepository.traceList } returns TestDataConstants.getTraceList()

        useCase.addLocation2currentRecord(arrayListOf(TestDataConstants.getStepTraceLocation()))
        verify(exactly = 1) { runBlocking { traceRecordRepository.insertOrUpdateLocation(any(), any()) } }

        // 停止并更新记录
        runBlocking { useCase.stopTrace() }
        verify(exactly = 1) { runBlocking { locationRepository.stopTrace() } }
        verify(exactly = 1) { runBlocking { traceRecordRepository.updateByTraceList(any()) } }
        verify(exactly = 1) { runBlocking { locationRepository.clearTraceList() } }
    }

}