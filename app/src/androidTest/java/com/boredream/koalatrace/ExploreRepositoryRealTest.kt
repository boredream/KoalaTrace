package com.boredream.koalatrace

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.boredream.koalatrace.data.constant.CommonConstant
import com.boredream.koalatrace.data.repo.ExploreRepository
import com.boredream.koalatrace.data.repo.source.ExploreLocalDataSource
import com.boredream.koalatrace.data.repo.source.ExploreRemoteDataSource
import com.boredream.koalatrace.db.AppDatabase
import com.boredream.koalatrace.utils.DataStoreUtils
import com.boredream.koalatrace.utils.PrintLogger
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class ExploreRepositoryRealTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var context: Context
    private lateinit var repository: ExploreRepository

    @Before
    fun init() {
        hiltRule.inject()

        context = ApplicationProvider.getApplicationContext()
        DataStoreUtils.init(context)

        val db = Room.databaseBuilder(context, AppDatabase::class.java, CommonConstant.DB_NAME)
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .build()

        repository = ExploreRepository(PrintLogger(),
            ExploreLocalDataSource(PrintLogger(), db),
            ExploreRemoteDataSource(PrintLogger(), context, Dispatchers.Default))
    }

    @Test
    fun testGetDistrictInfo() = runBlocking {
        val areaInfo = repository.getAreaInfo("长宁区")
        println("areaInfo = $areaInfo")
    }

}