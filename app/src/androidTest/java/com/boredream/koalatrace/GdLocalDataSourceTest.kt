package com.boredream.koalatrace

import android.content.Context
import android.os.SystemClock
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.blankj.utilcode.util.TimeUtils
import com.boredream.koalatrace.data.TraceRecordArea
import com.boredream.koalatrace.data.constant.CommonConstant
import com.boredream.koalatrace.data.constant.LocationParam
import com.boredream.koalatrace.data.repo.BackupRepository
import com.boredream.koalatrace.data.repo.source.GdLocationDataSource
import com.boredream.koalatrace.data.repo.source.TraceRecordLocalDataSource
import com.boredream.koalatrace.db.AppDatabase
import com.boredream.koalatrace.utils.DataStoreUtils
import com.boredream.koalatrace.utils.PrintLogger
import com.boredream.koalatrace.utils.TraceUtils
import com.boredream.koalatrace.utils.TraceUtils.createLineBuffer
import com.google.gson.Gson
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryCollection
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class GdLocalDataSourceTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var context: Context
    private lateinit var dataSource: GdLocationDataSource

    @Before
    fun init() {
        hiltRule.inject()

        context = ApplicationProvider.getApplicationContext()
        DataStoreUtils.init(context)

        dataSource = GdLocationDataSource(context, Dispatchers.IO, LocationParam())
    }

    @Test
    fun testGetDistrictArea() = runBlocking {
        val districtResult = dataSource.getDistrictArea("长宁区")!!
        println(districtResult.district[0])
        println(districtResult.district[0].districtBoundary()[0])
    }

}