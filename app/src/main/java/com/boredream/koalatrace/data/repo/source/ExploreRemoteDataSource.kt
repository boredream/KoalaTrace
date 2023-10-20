package com.boredream.koalatrace.data.repo.source

import android.content.Context
import com.amap.api.services.district.DistrictItem
import com.amap.api.services.district.DistrictResult
import com.amap.api.services.district.DistrictSearch
import com.amap.api.services.district.DistrictSearchQuery
import com.blankj.utilcode.util.CollectionUtils
import com.boredream.koalatrace.utils.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExploreRemoteDataSource @Inject constructor(
    private val logger: Logger,
    @ApplicationContext val context: Context,
    private val dispatcher: CoroutineDispatcher,
) {

    suspend fun getDistrictInfo(keywords: String): DistrictItem? = withContext(dispatcher) {
        // Retrofit 会自动处理线程问题，这里是使用了高德的远程接口，需要自行处理
        val query = DistrictSearchQuery()
        query.keywords = keywords
        query.isShowBoundary = true
        query.subDistrict = 0 // 获取几层子区域信息

        val search = DistrictSearch(context)
        search.query = query
        val result = search.searchDistrict()

        if(result != null && CollectionUtils.isNotEmpty(result.district)) {
            result.district[0]
        } else {
            null
        }
    }


}