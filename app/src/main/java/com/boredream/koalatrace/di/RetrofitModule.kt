package com.boredream.koalatrace.di

import com.boredream.koalatrace.data.constant.DataStoreKey
import com.boredream.koalatrace.net.ApiService
import com.boredream.koalatrace.net.ServiceCreator
import com.boredream.koalatrace.utils.DataStoreUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Singleton
    @Provides
    fun provideApiService(): ApiService {
        ServiceCreator.tokenFactory = { DataStoreUtils.readStringData(DataStoreKey.TOKEN, "") }
        return ServiceCreator.create(ApiService::class.java)
    }

}