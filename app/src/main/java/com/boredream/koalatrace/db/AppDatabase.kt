package com.boredream.koalatrace.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.db.dao.TraceLocationDao
import com.boredream.koalatrace.db.dao.TraceRecordDao

@Database(entities = [TraceRecord::class, TraceLocation::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun traceRecordDao(): TraceRecordDao

    abstract fun traceLocationDao(): TraceLocationDao

}