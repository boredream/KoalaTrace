package com.boredream.koalatrace.data.repo

import com.amap.api.maps.model.LatLng
import com.boredream.koalatrace.base.BaseRepository
import com.boredream.koalatrace.data.ExploreAreaInfo
import com.boredream.koalatrace.data.repo.source.ExploreLocalDataSource
import com.boredream.koalatrace.data.repo.source.ExploreRemoteDataSource
import com.boredream.koalatrace.utils.Logger
import com.boredream.koalatrace.utils.TraceUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 区域探索
 * TODO 探索信息是记录到db呢，还是每次实时计算？
 */
@Singleton
class ExploreRepository @Inject constructor(
    private val logger: Logger,
    private val localDataSource: ExploreLocalDataSource,
    private val remoteDataSource: ExploreRemoteDataSource,
) : BaseRepository() {

    suspend fun getAreaInfo(keywords: String) = withContext(Dispatchers.Default) {
        // 获取行政区划边界信息后，还需要进行一系列计算，计算逻辑放在 Dispatchers.Default
        var areaInfo = localDataSource.getAreaInfo(keywords)
        if(areaInfo == null) {
            remoteDataSource.getDistrictInfo(keywords)?.let { district ->
                // TODO: district boundary 是数组，代表一个区域可能是多个形状？
                val boundaryStr = district.districtBoundary()[0]
                val boundary = TraceUtils.str2LatLngList(boundaryStr)
                // TODO: parentAreaCode
                areaInfo = ExploreAreaInfo(keywords, "", boundaryStr)
                areaInfo!!.blockList = TraceUtils.splitCityDistinct(keywords, boundary)

                localDataSource.saveAreaInfo(areaInfo!!)
                logger.i("get from remote $keywords")
            }
        }
        areaInfo
    }

}