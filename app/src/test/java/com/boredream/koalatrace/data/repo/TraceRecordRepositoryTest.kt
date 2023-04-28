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

    @MockK(relaxed = true)
    private lateinit var remoteDataSource: TraceRecordRemoteDataSource

    @MockK(relaxed = true)
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
    fun `should save trace record`() = runTest {

    }

}