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
                // do nothing
            }
        }

        val dbName = CommonConstant.DB_NAME
        return Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .addMigrations(migration1To2)
            .build()
    }
}