package com.boredream.koalatrace.data.usecase

import com.boredream.koalatrace.base.BaseUseCase
import com.boredream.koalatrace.data.ResponseEntity
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.TraceRecordArea
import com.boredream.koalatrace.data.constant.LocationParam
import com.boredream.koalatrace.data.repo.ExploreRepository
import com.boredream.koalatrace.data.repo.LocationRepository
import com.boredream.koalatrace.data.repo.SensorRepository
import com.boredream.koalatrace.data.repo.TraceRecordRepository
import com.boredream.koalatrace.utils.Logger
import com.boredream.koalatrace.utils.TraceUtils
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExploreUseCase @Inject constructor(
    private val logger: Logger,
    private val locationParam: LocationParam,
    private val locationRepository: LocationRepository,
    private val traceRecordRepository: TraceRecordRepository,
    private val exploreRepository: ExploreRepository,
    private val scope: CoroutineScope,
) : BaseUseCase() {

    suspend fun calculateAreaExplore(keywords: String): ResponseEntity<TraceRecord> {
        // 计算一个区域的探索情况
        val areaInfo = exploreRepository.getAreaInfo(keywords)

        // 获取这个区域的所有轨迹
        val traceRecordList = traceRecordRepository.getListByCondition(
            recordArea = TraceRecordArea("上海市", "长宁区"),
            needLocationList = true)

        // TODO: can't get
        val polygonList = TraceUtils.genExplorePolygon(traceRecordList.data!!)


    }

}