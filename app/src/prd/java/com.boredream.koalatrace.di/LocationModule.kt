package com.boredream.koalatrace.di

import com.boredream.koalatrace.data.constant.LocationParam
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    fun provideLocationParam(): LocationParam {
        return LocationParam()
    }

}