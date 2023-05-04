package com.boredream.koalatrace

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.amap.api.mapcore.util.it
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.ZipUtils
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.constant.CommonConstant
import com.boredream.koalatrace.data.repo.BackupRepository
import com.boredream.koalatrace.db.AppDatabase
import com.boredream.koalatrace.utils.DataStoreUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File


@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class DbTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repo: BackupRepository

    @Before
    fun init() {
        hiltRule.inject()

        context = ApplicationProvider.getApplicationContext()
        DataStoreUtils.init(context)

        db = Room.databaseBuilder(context, AppDatabase::class.java, CommonConstant.DB_NAME)
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .build()
        repo = BackupRepository(PrintLogger(), db)
    }

    @Test
    fun testPrintAllDb() = runBlocking {
        val dao = db.traceRecordDao()
        val locationDao = db.traceLocationDao()
        dao.loadAll().forEach {
            println("-------------------")
            val list = locationDao.loadByTraceRecordId(it.id)
            println("$it , list = " + list.size)
        }
    }

    @Test
    fun testDbToCustomerFile() = runBlocking {
        val dao = db.traceRecordDao()
        val locationDao = db.traceLocationDao()
        val list = dao.loadAll()
        list.forEach {
            println("-------------------")
            val traceList = locationDao.loadByTraceRecordId(it.id)
            it.traceList = ArrayList(traceList)
            println("$it , list = " + traceList.size)
        }
        val json = Gson().toJson(list)
        val file = File(PathUtils.getInternalAppCachePath(), "trace.json")
        FileIOUtils.writeFileFromString(file, json)
        ZipUtils.zipFile(file, File(PathUtils.getExternalStoragePath(), "trace.zip"))
        println("done")
    }

    @Test
    fun testCustomerFileToDb() = runBlocking {
        val dao = db.traceRecordDao()
        val locationDao = db.traceLocationDao()
        dao.deleteAll()
        locationDao.deleteAll()

        val file = File(PathUtils.getExternalStoragePath() + "/KoalaTrace/trace_new.json")
        val json = FileIOUtils.readFile2String(file)

        val itemType = object : TypeToken<List<TraceRecord>>() {}.type
        val traceList = Gson().fromJson<List<TraceRecord>>(json, itemType)

        traceList.forEach { record ->
            val id = dao.insertOrUpdate(record)
            record.traceList.forEach { it.traceId = id }
            locationDao.insertOrUpdateAll(record.traceList)
        }

        println("done")
    }

    @Test
    fun restoreDb() = runBlocking {
        val dao = db.traceRecordDao()
        dao.deleteAll()
        var list = dao.loadAll()
        val oldListSize = list.size

        // 先备份旧的
        val backupSuccess = repo.backup()
        Assert.assertEquals(true, backupSuccess)

        // 插入数据
        val record = TraceRecord("测试备份", 0, 0, 0, isRecording = true)
        dao.insertOrUpdate(record)
        println(record)

        list = dao.loadAll()
        Assert.assertEquals(oldListSize + 1, list.size)

        // 然后恢复数据
        val restoreSuccess = repo.restore()
        Assert.assertEquals(true, restoreSuccess)

        list = dao.loadAll()
        Assert.assertEquals(oldListSize, list.size)
    }

}
