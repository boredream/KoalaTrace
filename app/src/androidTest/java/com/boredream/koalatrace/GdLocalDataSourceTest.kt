package com.boredream.koalatrace

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.boredream.koalatrace.data.constant.LocationParam
import com.boredream.koalatrace.data.repo.source.GdLocationDataSource
import com.boredream.koalatrace.utils.DataStoreUtils
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class GdLocalDataSourceTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var context: Context
    private lateinit var dataSource: GdLocationDataSource

    @Before
    fun init() {
        hiltRule.inject()

        context = ApplicationProvider.getApplicationContext()
        DataStoreUtils.init(context)

        dataSource = GdLocationDataSource(context, LocationParam())
    }

    @Test
    fun testGetDistrictArea() = runBlocking {
//        val districtResult = dataSource.getDistrictArea("长宁区")!!
//        println(districtResult.district[0])
//        println(districtResult.district[0].districtBoundary()[0])
    }

}