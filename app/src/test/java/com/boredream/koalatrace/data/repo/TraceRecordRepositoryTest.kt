package com.boredream.koalatrace.data.repo

import com.boredream.koalatrace.PrintLogger
import com.boredream.koalatrace.data.ResponseEntity
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.repo.source.ConfigLocalDataSource
import com.boredream.koalatrace.data.repo.source.TraceRecordLocalDataSource
import com.boredream.koalatrace.data.repo.source.TraceRecordRemoteDataSource
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TraceRecordRepositoryTest {

    @MockK
    private lateinit var configDataSource: ConfigLocalDataSource

    @MockK
    private lateinit var remoteDataSource: TraceRecordRemoteDataSource

    @MockK
    private lateinit var localDataSource: TraceRecordLocalDataSource

    private lateinit var repo: TraceRecordRepository

    private val record = TraceRecord("", 0L, 0L, 0)

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repo = TraceRecordRepository(
            PrintLogger(),
            configDataSource,
            remoteDataSource,
            localDataSource
        )

        // write stub
        every { configDataSource.set(any(), any()) } just runs
        every { runBlocking { localDataSource.add(any()) } } returns
                ResponseEntity.success(record)
        every { runBlocking { localDataSource.update(any()) } } returns
                ResponseEntity.success(record)
        every { runBlocking { remoteDataSource.add(any()) } } returns
                ResponseEntity.success(record)
        every { runBlocking { remoteDataSource.update(any()) } } returns
                ResponseEntity.success(record)
    }

    @Test
    fun testSyncDataPull_noSync() = runTest {
        // 没有待同步的数据
        stub(arrayListOf(), null)
        repo.syncDataPull()
        verify { runBlocking { configDataSource.getLong(any()) } }
        verify { runBlocking { remoteDataSource.getTraceRecordListBySyncTimestamp(any()) } }
    }

    @Test
    fun testSyncDataPull_syncNoLocal() = runTest {
        // 有一个待同步的数据，但是无本地匹配。则添加到本地
        val record = TraceRecord("", 0L, 0L, 0)
        record.id = "110"

        val traceRecordListFromRemoteSync = arrayListOf(record)
        val localRecord: TraceRecord? = null

        stub(traceRecordListFromRemoteSync, localRecord)

        repo.syncDataPull()

        verify { runBlocking { configDataSource.getLong(any()) } }
        verify { runBlocking { remoteDataSource.getTraceRecordListBySyncTimestamp(any()) } }
        verify { runBlocking { localDataSource.getTraceRecordByDbId(any()) } }
        verify { runBlocking { remoteDataSource.getTraceLocationList(any()) } }
        verify { runBlocking { localDataSource.add(any()) } }
        verify(exactly = 0) { runBlocking { configDataSource.set(any(), any()) } }
    }

    @Test
    fun testSyncDataPull_syncOverrideLocal() = runTest {
        // 有一个待同步的数据，且本地有匹配，匹配的数据已同步。则覆盖到本地
        val record = TraceRecord("", 0L, 0L, 0)
        record.syncTimestamp = 1000
        record.id = "110"

        val traceRecordListFromRemoteSync = arrayListOf(record)
        val localRecord = TraceRecord("", 0L, 0L, 0)
        localRecord.synced = true

        stub(traceRecordListFromRemoteSync, localRecord)

        repo.syncDataPull()

        verify { runBlocking { configDataSource.getLong(any()) } }
        verify { runBlocking { remoteDataSource.getTraceRecordListBySyncTimestamp(any()) } }
        verify { runBlocking { localDataSource.getTraceRecordByDbId(any()) } }
        verify { runBlocking { remoteDataSource.getTraceLocationList(any()) } }
        verify { runBlocking { localDataSource.add(any()) } }
        verify(exactly = 1) { runBlocking { configDataSource.set(any(), any()) } }
    }

    @Test
    fun testSyncDataPull_syncUseLocal() = runTest {
        // 有一个待同步的数据，且本地有匹配，匹配的数据未同步。抛弃服务端数据
        val record = TraceRecord("", 0L, 0L, 0)
        record.id = "110"

        val traceRecordListFromRemoteSync = arrayListOf(record)
        val localRecord = TraceRecord("", 0L, 0L, 0)
        localRecord.synced = false

        stub(traceRecordListFromRemoteSync, localRecord)

        repo.syncDataPull()

        verify { runBlocking { configDataSource.getLong(any()) } }
        verify { runBlocking { remoteDataSource.getTraceRecordListBySyncTimestamp(any()) } }
        verify { runBlocking { localDataSource.getTraceRecordByDbId(any()) } }
        verify(exactly = 0) { runBlocking { remoteDataSource.getTraceLocationList(any()) } }
        verify(exactly = 0) { runBlocking { localDataSource.add(any()) } }
        verify(exactly = 0) { runBlocking { configDataSource.set(any(), any()) } }
    }

    @Test
    fun testSyncDataPush_noSync() = runTest {
        // 没有待同步的数据
        every { runBlocking { localDataSource.getUnSyncedTraceRecord() } } returns
                ResponseEntity.success(arrayListOf())

        repo.syncDataPush()
        verify(exactly = 0) { runBlocking { remoteDataSource.update(any()) } }
        verify(exactly = 0) { runBlocking { remoteDataSource.add(any()) } }
    }

    @Test
    fun testSyncDataPush_sync() = runTest {
        // 有需要同步的数据
        val unSyncRecord = TraceRecord("", 0L, 0L, 0)
        every { runBlocking { localDataSource.getUnSyncedTraceRecord() } } returns
                ResponseEntity.success(arrayListOf(unSyncRecord))

        val record = TraceRecord("", 0L, 0L, 0)
        record.syncTimestamp = 1000
        every { runBlocking { remoteDataSource.add(any()) } } returns
                ResponseEntity.success(record)

        stub(arrayListOf(), null)
        repo.syncDataPush()
        verify { runBlocking { remoteDataSource.add(any()) } }
        verify { runBlocking { localDataSource.update(any()) } }
        verify { runBlocking { configDataSource.set(any(), any()) } }
    }

    private fun stub(
        traceRecordListFromRemoteSync: ArrayList<TraceRecord>,
        localRecordByDbId: TraceRecord?
    ) {
        every { configDataSource.getLong(any()) } returns 0L
        every { runBlocking { remoteDataSource.getTraceRecordListBySyncTimestamp(any()) } } returns
                ResponseEntity.success(traceRecordListFromRemoteSync)
        every { runBlocking { localDataSource.getTraceRecordByDbId(any()) } } returns
                ResponseEntity.success(localRecordByDbId)
        every { runBlocking { remoteDataSource.getTraceLocationList(any()) } } returns
                ResponseEntity.success(arrayListOf())
        every { runBlocking { localDataSource.getTraceLocationList(any()) } } returns
                ResponseEntity.success(arrayListOf())
    }

}