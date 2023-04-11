package com.boredream.koalatrace.base

import com.boredream.koalatrace.data.ResponseEntity
import com.boredream.koalatrace.data.dto.PageResultDto

/**
 * 请求类 Repo
 */
abstract class BaseRequestRepository<T : BaseEntity> : BaseRepository() {

    var cacheListPage = 1
    var cacheList: ArrayList<T> = ArrayList()
    var cacheListCanLoadMore = false
    var cacheIsDirty: Boolean
        get() = RepoCacheHelper.repoCache[javaClass.simpleName] ?: true
        set(value) {
            RepoCacheHelper.repoCache[javaClass.simpleName] = value
        }

    protected suspend fun getPageList(
        forceRemote: Boolean = false,
        loadMore: Boolean = false,
        request: suspend (page: Int) -> ResponseEntity<PageResultDto<T>>
    ): ResponseEntity<ArrayList<T>> {
        if (!forceRemote && !cacheIsDirty && !loadMore) {
            // 非强制远程，且缓存数据有效，且是非加载更多模式时，直接返回
            return ResponseEntity.success(cacheList)
        }

        val requestPage = if (loadMore) (cacheListPage + 1) else 1
        val response: ResponseEntity<PageResultDto<T>> =
            tryHttpError { request.invoke(requestPage) }
        if (response.isSuccess()) {
            // TODO: db local data source
            // 缓存在本地
            val responseData = response.getSuccessData()
            cacheListPage = requestPage
            if (!loadMore) cacheList.clear()
            cacheList.addAll(responseData.records)
            cacheListCanLoadMore = responseData.records.size == 20
            cacheIsDirty = false
        }

        // 数据转换一层，PageResult -> ListResult
        return ResponseEntity(
            cacheList,
            response.code,
            response.msg
        )
    }

    protected suspend fun getList(
        forceRemote: Boolean = false,
        request: suspend () -> ResponseEntity<ArrayList<T>>
    ): ResponseEntity<ArrayList<T>> {
        if (!forceRemote && !cacheIsDirty) {
            // 非强制远程，且缓存数据有效时，直接返回
            return ResponseEntity.success(cacheList)
        }

        val response: ResponseEntity<ArrayList<T>> = tryHttpError { request.invoke() }
        if (response.isSuccess()) {
            // TODO: db local data source
            // 缓存在本地
            cacheList.clear()
            cacheList.addAll(response.getSuccessData())
            cacheIsDirty = false
        }

        // 数据转换一层，List -> ListResult
        return ResponseEntity(
            cacheList,
            response.code,
            response.msg
        )
    }

    /**
     * 提交请求
     * @param request SuspendFunction0<ResponseEntity<Boolean>> 请求
     * @return ResponseEntity<Boolean> 相应
     */
    protected suspend fun commit(
        request: suspend () -> ResponseEntity<Boolean>
    ): ResponseEntity<Boolean> {
        val response: ResponseEntity<Boolean> = tryHttpError { request.invoke() }
        if (response.isSuccess()) {
            // 修改成功后，缓存需要重新刷新
            cacheIsDirty = true
        }
        return response
    }

    /**
     * 删除请求
     * @param request SuspendFunction0<ResponseEntity<Boolean>> 请求
     * @return ResponseEntity<Boolean> 相应
     */
    protected suspend fun commitDelete(
        id: String,
        request: suspend () -> ResponseEntity<Boolean>
    ): ResponseEntity<Boolean> {
        val response: ResponseEntity<Boolean> = tryHttpError { request.invoke() }
        if (response.isSuccess()) {
            // 删除成功后，无需刷新缓存，直接删掉对应数据即可
            var deleteItem: T? = null
            for (item in cacheList) {
                if (id == item.id) {
                    deleteItem = item
                    break
                }
            }
            deleteItem?.let { cacheList.remove(it) }
        }
        return response
    }

    /**
     * http请求预处理
     */
    private suspend fun <T> tryHttpError(request: suspend () -> ResponseEntity<T>): ResponseEntity<T> {
        return try {
            request.invoke()
        } catch (e: Exception) {
            ResponseEntity.httpError(e)
        }
    }

}