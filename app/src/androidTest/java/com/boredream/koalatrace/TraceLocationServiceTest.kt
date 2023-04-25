package com.boredream.koalatrace

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.blankj.utilcode.util.TimeUtils
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.constant.GlobalConstant
import com.boredream.koalatrace.data.repo.LocationRepository
import com.boredream.koalatrace.data.repo.TraceRecordRepository
import com.boredream.koalatrace.data.repo.source.*
import com.boredream.koalatrace.data.usecase.TraceUseCase
import com.boredream.koalatrace.db.AppDatabase
import com.boredream.koalatrace.utils.DataStoreUtils
import com.google.gson.Gson
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class TraceEditMapActivityTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var logger: PrintLogger
    private lateinit var locationRepository: LocationRepository
    private lateinit var traceRecordRepository: TraceRecordRepository
    private lateinit var traceUseCase: TraceUseCase

    @Before
    fun init() {
        hiltRule.inject()

        val context = ApplicationProvider.getApplicationContext<Context>()
        DataStoreUtils.init(context)

        logger = PrintLogger()
        val dataSource = mockk<LocationDataSource>(relaxed = true)
        val remoteDataSource = mockk<TraceRecordRemoteDataSource>()
        val configDataSource = mockk<ConfigLocalDataSource>()

        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val localDataSource = TraceRecordLocalDataSource(logger, db)
        locationRepository = LocationRepository(logger, dataSource)
        traceRecordRepository = TraceRecordRepository(
            logger, configDataSource, remoteDataSource, localDataSource
        )

        traceUseCase = TraceUseCase(PrintLogger(), locationRepository, traceRecordRepository)

        every { locationRepository.startLocation() } just runs
    }

    @Test
    fun insertLocation() = runBlocking {
        val time = System.currentTimeMillis()
        val timeStr = TimeUtils.millis2String(time)
        val title = "轨迹 $timeStr"
        val record = TraceRecord(title, time, time, 0, isRecording = true)
        traceRecordRepository.insertOrUpdate(record)

        val recordResponse = traceRecordRepository.getList().getSuccessData()[0]
        println(Gson().toJson(recordResponse))

        for (i in 1..10) {
            val location = TestDataConstants.getStepTraceLocation()
            traceRecordRepository.insertOrUpdateLocation(recordResponse.dbId, location)
        }

        val locationList = traceRecordRepository.getLocationList(null).getSuccessData()
        Assert.assertEquals(10, locationList.size)
    }

    @Test
    fun testAll() = runBlocking {
        traceUseCase.startLocation()
        traceUseCase.startTrace()

        Assert.assertNotNull(traceUseCase.currentTraceRecord)
        val list = traceRecordRepository.getList().getSuccessData()
        Assert.assertEquals(1, list.size)

        for (i in 1..10) {
            locationRepository.onLocationSuccess(TestDataConstants.getStepTraceLocation())
            traceUseCase.addLocation2currentRecord(arrayListOf(TestDataConstants.getStepTraceLocation()))

            val locationList = traceRecordRepository
                .getLocationList(traceUseCase.currentTraceRecord?.dbId).getSuccessData()
            Assert.assertEquals(i, locationList.size)
        }

        Assert.assertNotNull(traceUseCase.currentTraceRecord)
        Assert.assertEquals(10, traceUseCase.currentTraceRecord?.traceList?.size)
        Assert.assertEquals(10, locationRepository.traceList.size)
        traceUseCase.stopTrace()

        val locationList = traceRecordRepository.getLocationList(null).getSuccessData()
        Assert.assertEquals(10, locationList.size)
    }

}
