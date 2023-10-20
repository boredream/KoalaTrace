package com.boredream.koalatrace

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.boredream.koalatrace.data.constant.LocationParam
import com.boredream.koalatrace.data.repo.source.ExploreRemoteDataSource
import com.boredream.koalatrace.data.repo.source.GdLocationDataSource
import com.boredream.koalatrace.utils.DataStoreUtils
import com.boredream.koalatrace.utils.PrintLogger
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class ExploreRemoteDataSourceTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var context: Context
    private lateinit var dataSource: ExploreRemoteDataSource

    @Before
    fun init() {
        hiltRule.inject()

        context = ApplicationProvider.getApplicationContext()
        DataStoreUtils.init(context)

        dataSource = ExploreRemoteDataSource(PrintLogger(), context, Dispatchers.Default)
    }

    @Test
    fun testGetDistrictInfo() = runBlocking {
        // TODO: 如果区重名怎么办？
        val districtInfo = dataSource.getDistrictInfo("长宁区")!!
        println(districtInfo)
    }

}