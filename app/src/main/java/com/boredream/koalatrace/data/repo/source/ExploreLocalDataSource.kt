package com.boredream.koalatrace.data.repo.source

import androidx.room.Transaction
import com.boredream.koalatrace.data.ExploreAreaInfo
import com.boredream.koalatrace.db.AppDatabase
import com.boredream.koalatrace.utils.Logger
import javax.inject.Inject

class ExploreLocalDataSource @Inject constructor(
    private val logger: Logger,
    appDatabase: AppDatabase
) {

    private val dao = appDatabase.exploreInfoDao()

    @Transaction
    suspend fun getAreaInfo(keywords: String): ExploreAreaInfo? {
//        val areaInfo = dao.loadExploreArea(keywords)
//        if(areaInfo != null) {
//            areaInfo.blockList = dao.loadExploreBlockList(keywords)
//            return areaInfo
//        }
        return null
    }

    @Transaction
    suspend fun saveAreaInfo(areaInfo: ExploreAreaInfo) {
//        val add = dao.insertOrUpdateArea(areaInfo)
//        if(add != -1L) {
//            dao.insertOrUpdateBlock(areaInfo.blockList)
//        }
    }

}