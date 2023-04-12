package com.boredream.koalatrace.di

import com.boredream.koalatrace.data.repo.source.FakeLocationDataSource
import com.boredream.koalatrace.data.repo.source.GdLocationDataSource
import com.boredream.koalatrace.data.repo.source.LocationDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class RepoModule {

    @Binds
    abstract fun provideLocationDataSource(repo: FakeLocationDataSource): LocationDataSource
//
//    @Binds
//    abstract fun provideLocationDataSource(repo: GdLocationDataSource): LocationDataSource

}