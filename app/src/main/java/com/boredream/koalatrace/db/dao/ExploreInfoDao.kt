package com.boredream.koalatrace.db.dao

import androidx.room.*
import com.boredream.koalatrace.data.ExploreAreaInfo
import com.boredream.koalatrace.data.ExploreBlockInfo

@Dao
interface ExploreInfoDao {

    @Query("SELECT * FROM ExploreAreaInfo WHERE areaCode = :areaCode")
    suspend fun loadExploreArea(areaCode: String): ExploreAreaInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateArea(info: ExploreAreaInfo): Long

    @Delete
    suspend fun deleteArea(data: ExploreAreaInfo): Int


    @Query("SELECT * FROM ExploreBlockInfo WHERE areaCode = :areaCode")
    suspend fun loadExploreBlockList(areaCode: String): List<ExploreBlockInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBlock(info: ExploreBlockInfo): Long

    @Delete
    suspend fun deleteBlock(data: ExploreBlockInfo): Int

}