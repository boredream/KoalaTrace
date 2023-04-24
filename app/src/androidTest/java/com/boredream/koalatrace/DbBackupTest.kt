package com.boredream.koalatrace

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.constant.CommonConstant
import com.boredream.koalatrace.data.repo.BackupRepository
import com.boredream.koalatrace.db.AppDatabase
import com.boredream.koalatrace.utils.DataStoreUtils
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class DbBackupTest {

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
