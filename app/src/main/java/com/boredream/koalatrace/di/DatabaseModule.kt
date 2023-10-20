package com.boredream.koalatrace.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.boredream.koalatrace.data.constant.CommonConstant
import com.boredream.koalatrace.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val migration1To2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE TraceRecord ADD COLUMN `country` TEXT")
                database.execSQL("ALTER TABLE TraceRecord ADD COLUMN `adminArea` TEXT")
                database.execSQL("ALTER TABLE TraceRecord ADD COLUMN `subAdminArea` TEXT")
                database.execSQL("ALTER TABLE TraceRecord ADD COLUMN `locality` TEXT")
                database.execSQL("ALTER TABLE TraceRecord ADD COLUMN `subLocality` TEXT")
            }
        }
//        val migration2To3 = object : Migration(2, 3) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("CREATE TABLE IF NOT EXISTS ExploreAreaInfo (\n" +
//                        "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
//                        "    areaCode TEXT NOT NULL,\n" +
//                        "    parentAreaCode TEXT NOT NULL,\n" +
//                        "    explorePercent REAL NOT NULL\n" +
//                        ")")
//                database.execSQL("CREATE TABLE IF NOT EXISTS ExploreBlockInfo (\n" +
//                        "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
//                        "    areaCode TEXT NOT NULL,\n" +
//                        "    rectBoundary TEXT NOT NULL,\n" +
//                        "    actualBoundary TEXT NOT NULL,\n" +
//                        "    actualArea REAL NOT NULL,\n" +
//                        "    explorePercent REAL NOT NULL\n" +
//                        ")")
//            }
//        }

        val dbName = CommonConstant.DB_NAME
        return Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .addMigrations(migration1To2)
//            .addMigrations(migration2To3)
            .build()
    }
}