package com.boredream.koalatrace.di

import com.boredream.koalatrace.data.repo.source.GdLocationDataSource
import com.boredream.koalatrace.data.repo.source.LocationDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    abstract fun provideLocationDataSource(repo: GdLocationDataSource): LocationDataSource

}