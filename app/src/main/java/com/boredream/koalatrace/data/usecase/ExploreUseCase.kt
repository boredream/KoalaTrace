package com.boredream.koalatrace.data.usecase

import android.os.SystemClock
import com.blankj.utilcode.util.CollectionUtils
import com.blankj.utilcode.util.StringUtils
import com.boredream.koalatrace.base.BaseUseCase
import com.boredream.koalatrace.data.ExploreAreaInfo
import com.boredream.koalatrace.data.ExploreBlockInfo
import com.boredream.koalatrace.data.ResponseEntity
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.TraceRecordArea
import com.boredream.koalatrace.data.constant.LocationParam
import com.boredream.koalatrace.data.repo.ExploreRepository
import com.boredream.koalatrace.data.repo.LocationRepository
import com.boredream.koalatrace.data.repo.TraceRecordRepository
import com.boredream.koalatrace.utils.JtsUtils
import com.boredream.koalatrace.utils.Logger
import com.boredream.koalatrace.utils.TraceUtils
import kotlinx.coroutines.CoroutineScope
import org.locationtech.jts.geom.Polygon
import java.util.Calendar
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

    suspend fun loadArea() = traceRecordRepository.loadArea()

    // 计算一个区域的探索情况
    suspend fun calculateAreaExplore(area: TraceRecordArea): ResponseEntity<ExploreAreaInfo> {
        // 获取区域信息
        val keywords = area.locality!!
        val areaInfo = exploreRepository.getAreaInfo(keywords)
        // 把区域按方案分割
        val blockInfoList = TraceUtils.splitDistinctToBlockList(keywords, areaInfo.boundaryLatLngList)

        // 获取这个区域的所有轨迹
        // FIXME: for test more quick
        val startCalendar = Calendar.getInstance()
        startCalendar.add(Calendar.DAY_OF_YEAR, -30)

        val traceRecordList = traceRecordRepository.getListByCondition(
            recordArea = area,
//            startTime = startCalendar.timeInMillis,
//            endTime = Calendar.getInstance().timeInMillis,
            needLocationList = true
        )
        // 按轨迹生成探索形状
        val explorePolygonList = TraceUtils.genExplorePolygon(traceRecordList.data!!)

        // 探索形状和整个边界形状，做一次交集
        val polygonList = arrayListOf<Polygon>()
        val boundaryPolygon = JtsUtils.str2Polygon(areaInfo.boundary)
        for (explorePolygon in explorePolygonList) {
            val intersection = boundaryPolygon.intersection(explorePolygon)
            polygonList.addAll(JtsUtils.geometry2polygonList(intersection))
        }
        areaInfo.explorePolygon = polygonList

        // 区域方格和探索轨迹取交集 TODO 算法优化
        val startTime = SystemClock.elapsedRealtime()
        blockInfoList.forEach { blockInfo ->
            blockInfo.explorePercent = 0.0
            explorePolygonList.forEach { polygon ->
                val actualBoundaryStrList = blockInfo.actualBoundary.split("==")
                actualBoundaryStrList.forEach {
                    val blockActualPolygon = JtsUtils.str2Polygon(it)
                    val intersection = polygon.intersection(blockActualPolygon)
                    if (!intersection.isEmpty) {
                        blockInfo.explorePercent += (intersection.area / blockActualPolygon.area)
                    }
                }
            }
        }
        logger.v("calculateAreaExplore core code duration = ${SystemClock.elapsedRealtime() - startTime}")
        areaInfo.blockList = blockInfoList
        return ResponseEntity.success(areaInfo)
    }

}