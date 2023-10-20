package com.boredream.koalatrace.data.repo

import com.amap.api.maps.model.LatLng
import com.boredream.koalatrace.base.BaseRepository
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

    suspend fun get(keywords: String) = withContext(Dispatchers.Default) {
        // 获取行政区划边界信息后，还需要进行一系列计算，计算逻辑放在 Dispatchers.Default
        val areaInfo = localDataSource.getAreaInfo(keywords)
        if(areaInfo == null) {
            remoteDataSource.getDistrictInfo(keywords)?.let {
                // TODO: district boundary 是数组，代表一个区域可能是多个形状？
                val boundary = arrayListOf<LatLng>()
                // districtBoundary 的格式为 121.41257,31.191118;121.412083,31.191139; ...
                for (coordinate in it.districtBoundary()[0].split(";")) {
                    boundary.add(
                        LatLng(
                            coordinate.split(',')[1].toDouble(),
                            coordinate.split(',')[0].toDouble()
                        )
                    )
                }
                val splitCityDistinct = TraceUtils.splitCityDistinct(boundary)
            }
        }
    }

}