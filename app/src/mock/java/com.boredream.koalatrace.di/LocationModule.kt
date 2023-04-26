package com.boredream.koalatrace.di

import android.text.format.DateUtils
import com.boredream.koalatrace.data.constant.LocationParam
import com.boredream.koalatrace.data.repo.source.FakeLocationDataSource
import com.boredream.koalatrace.data.repo.source.LocationDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    fun provideLocationParam(): LocationParam {
        return LocationParam(
            locationInterval = 2000L,
            stopThresholdDuration = DateUtils.SECOND_IN_MILLIS * 10,
            determineMoveCheckInterval = DateUtils.SECOND_IN_MILLIS * 30
        )
    }

}