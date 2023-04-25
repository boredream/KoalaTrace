package com.boredream.koalatrace.base

object RepoCacheHelper {
    val repoCache = HashMap<String, Boolean>()
}

/**
 * 数据仓库，单例，全局复用
 * https://developer.android.com/topic/architecture/data-layer?hl=zh-cn#create_the_repository
 */
open class BaseRepository {

}