package com.boredream.koalatrace

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.blankj.utilcode.util.TimeUtils
import com.boredream.koalatrace.data.constant.CommonConstant
import com.boredream.koalatrace.data.repo.BackupRepository
import com.boredream.koalatrace.data.repo.source.TraceRecordLocalDataSource
import com.boredream.koalatrace.db.AppDatabase
import com.boredream.koalatrace.utils.DataStoreUtils
import com.boredream.koalatrace.utils.PrintLogger
import com.google.gson.Gson
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class TraceRecordLocalDataSourceTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private val logger = PrintLogger()
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var dataSource: TraceRecordLocalDataSource

    @Before
    fun init() {
        hiltRule.inject()

        context = ApplicationProvider.getApplicationContext()
        DataStoreUtils.init(context)

        db = Room.databaseBuilder(context, AppDatabase::class.java, CommonConstant.DB_NAME)
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .build()

        dataSource = TraceRecordLocalDataSource(logger, db)
    }

    @Test
    fun testConditionDb() = runBlocking {
        val startDate = TimeUtils.string2Millis("2023-05-20", "yyyy-MM-dd")
        val endDate = TimeUtils.string2Millis("2023-05-24", "yyyy-MM-dd")
        val response = dataSource.getListByCondition(startDate, endDate)
        println(Gson().toJson(response))
    }

}