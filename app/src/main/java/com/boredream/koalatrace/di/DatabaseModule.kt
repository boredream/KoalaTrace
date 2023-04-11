package com.boredream.koalatrace.di

import android.content.Context
import androidx.room.Room
import com.boredream.koalatrace.data.constant.GlobalConstant
import com.boredream.koalatrace.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val dbNamePre = "love-book"

    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val user = GlobalConstant.getLocalUser()
        val dbName = "$dbNamePre-${user?.id}"
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            dbName,
        ).build()
    }
}