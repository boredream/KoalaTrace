package com.boredream.koalatrace.base

object RepoCacheHelper {
    val repoCache = HashMap<String, Boolean>()
}

/**
 * 数据仓库，单例，全局复用
 */
open class BaseRepository {

}