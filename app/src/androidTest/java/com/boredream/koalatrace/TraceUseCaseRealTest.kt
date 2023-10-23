package com.boredream.koalatrace

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider
import com.boredream.koalatrace.TestDataConstants
import com.boredream.koalatrace.TestDataConstants.locationParam
import com.boredream.koalatrace.data.ResponseEntity
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.constant.CommonConstant
import com.boredream.koalatrace.data.constant.LocationParam
import com.boredream.koalatrace.data.repo.ExploreRepository
import com.boredream.koalatrace.data.repo.LocationRepository
import com.boredream.koalatrace.data.repo.SensorRepository
import com.boredream.koalatrace.data.repo.TraceRecordRepository
import com.boredream.koalatrace.data.repo.source.ExploreLocalDataSource
import com.boredream.koalatrace.data.repo.source.ExploreRemoteDataSource
import com.boredream.koalatrace.data.repo.source.GdLocationDataSource
import com.boredream.koalatrace.data.repo.source.LocationDataSource
import com.boredream.koalatrace.data.repo.source.TraceRecordLocalDataSource
import com.boredream.koalatrace.data.usecase.ExploreUseCase
import com.boredream.koalatrace.data.usecase.TraceUseCase
import com.boredream.koalatrace.db.AppDatabase
import com.boredream.koalatrace.utils.PrintLogger
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
class TraceUseCaseRealTest {

    private lateinit var context: Context
    private lateinit var useCase: ExploreUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        context = ApplicationProvider.getApplicationContext()

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        val locationParam = LocationParam()

        val db = Room.databaseBuilder(context, AppDatabase::class.java, CommonConstant.DB_NAME)
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .build()

        val logger = PrintLogger()

        val locationRepository = LocationRepository(logger, GdLocationDataSource(context, locationParam))

        val traceRecordRepository = TraceRecordRepository(logger, TraceRecordLocalDataSource(logger, db))

        val exploreRepository = ExploreRepository(
            logger,
            ExploreLocalDataSource(logger, db),
            ExploreRemoteDataSource(logger, context, Dispatchers.Default)
        )

        useCase = ExploreUseCase(
            logger,
            locationParam,
            locationRepository,
            traceRecordRepository,
            exploreRepository,
            scope)
    }

    @Test
    fun testCalculateAreaExplore() = runTest {
        val area = useCase.calculateAreaExplore("长宁区")
        area.getSuccessData().blockList.forEach {
            println(it.explorePercent)
        }
    }

}