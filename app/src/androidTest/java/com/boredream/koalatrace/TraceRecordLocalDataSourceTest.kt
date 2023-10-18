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
import com.boredream.koalatrace.data.repo.BackupRepository
import com.boredream.koalatrace.data.repo.source.TraceRecordLocalDataSource
import com.boredream.koalatrace.db.AppDatabase
import com.boredream.koalatrace.utils.DataStoreUtils
import com.boredream.koalatrace.utils.PrintLogger
import com.boredream.koalatrace.utils.TraceUtils
import com.boredream.koalatrace.utils.TraceUtils.createLineBuffer
import com.google.gson.Gson
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
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
        val response = dataSource.getListByCondition(startDate, endDate, TraceRecordArea("上海市", "长宁区"))
        println(Gson().toJson(response))
    }

    @Test
    fun testLoadArea() = runBlocking {
        val response = dataSource.loadArea()
        println(Gson().toJson(response))
    }

    @Test
    fun testCalculateExploreArea() = runBlocking {
        val startTime = SystemClock.elapsedRealtime()

        // 测试探索区域
        val startDate = TimeUtils.string2Millis("2023-09-01", "yyyy-MM-dd")
        val endDate = TimeUtils.string2Millis("2023-10-18", "yyyy-MM-dd")
        val response = dataSource.getListByCondition(startDate, endDate, TraceRecordArea("上海市", "长宁区"))
        val list = response.data!!

//        list.forEach {
//            val listResponse = dataSource.getTraceLocationList(it.id)
//            if(listResponse.isSuccess()) {
//                it.traceList = listResponse.getSuccessData()
//            }
//        }
        println("load location list ${list.size} lines , duration = ${SystemClock.elapsedRealtime() - startTime}")

        val lineList = arrayListOf<LineString>()
        list.forEach { lineList.add(TraceUtils.simpleLine(it.traceList)) }
        println("simple ${list.size} lines , duration = ${SystemClock.elapsedRealtime() - startTime}")

        // 先 line-buffer
        val lineBufferList = arrayListOf<Geometry>()
        lineList.forEach { lineBufferList.add(createLineBuffer(it)) }
        println("line-buffer ${list.size} lines , duration = ${SystemClock.elapsedRealtime() - startTime}")

        // 后 merge
        val geometryCollection = GeometryFactory().buildGeometry(lineBufferList)
        var mergePolygon = geometryCollection
        if(geometryCollection is GeometryCollection) {
            mergePolygon = geometryCollection.union()
        }
        println("merge ${list.size} lines , duration = ${SystemClock.elapsedRealtime() - startTime}")
    }

}