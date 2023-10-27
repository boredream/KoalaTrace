package com.boredream.koalatrace.data.repo.source

import android.content.Context
import androidx.room.Transaction
import com.blankj.utilcode.util.SPUtils
import com.boredream.koalatrace.data.ExploreAreaInfo
import com.boredream.koalatrace.db.AppDatabase
import com.boredream.koalatrace.utils.Logger
import com.google.gson.Gson
import javax.inject.Inject

class ExploreLocalDataSource @Inject constructor(
    private val logger: Logger,
    appDatabase: AppDatabase,
) {

    private val dao = appDatabase.exploreInfoDao()
    private val sp: SPUtils = SPUtils.getInstance("test_sp")

    @Transaction
    suspend fun getAreaInfo(keywords: String): ExploreAreaInfo? {
        return Gson().fromJson(sp.getString(keywords), ExploreAreaInfo::class.java)
//        val areaInfo = dao.loadExploreArea(keywords)
//        if(areaInfo != null) {
//            areaInfo.blockList = dao.loadExploreBlockList(keywords)
//            return areaInfo
//        }
//        return null
    }

    @Transaction
    suspend fun saveAreaInfo(areaInfo: ExploreAreaInfo) {
        sp.put(areaInfo.areaCode, Gson().toJson(areaInfo))
//        val add = dao.insertOrUpdateArea(areaInfo)
//        if(add != -1L) {
//            dao.insertOrUpdateBlock(areaInfo.blockList)
//        }
    }

}