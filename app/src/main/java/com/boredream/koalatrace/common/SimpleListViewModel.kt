package com.boredream.koalatrace.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.boredream.koalatrace.base.BaseViewModel
import com.boredream.koalatrace.common.vmcompose.RefreshListVMCompose
import com.boredream.koalatrace.common.vmcompose.RequestVMCompose
import com.boredream.koalatrace.data.ResponseEntity
import com.boredream.koalatrace.vm.SingleLiveEvent

/**
 * ViewModel 负责连接视图和数据，主要的逻辑处理都在此进行。
 * 不能持有任何 Context / View 相关的应用。
 */
abstract class SimpleListViewModel<T> : BaseViewModel() {

    protected abstract fun isPageList(): Boolean

    protected open suspend fun repoListRequest(
        forceRemote: Boolean
    ): ResponseEntity<ArrayList<T>>? = null

    protected open suspend fun repoPageListRequest(
        loadMore: Boolean,
        forceRemote: Boolean
    ): ResponseEntity<ArrayList<T>>? = null

    protected abstract suspend fun repoDeleteRequest(
        data: T
    ): ResponseEntity<Boolean>

    val refreshListVMCompose = RefreshListVMCompose(viewModelScope)
    val deleteVMCompose = RequestVMCompose<Boolean>(viewModelScope)

    private val _toDetailEvent = SingleLiveEvent<Boolean>()
    val toDetailEvent: LiveData<Boolean> = _toDetailEvent

    /**
     * 每次页面重载时调用，非强制刷新，有缓存可直接再次使用
     */
    fun start() {
        if (isPageList()) {
            refreshListVMCompose.loadPageList(repoRequest = {
                repoPageListRequest(
                    loadMore = false,
                    forceRemote = false
                )!!
            })
        } else {
            refreshListVMCompose.loadList(repoRequest = { repoListRequest(false)!! })
        }
    }

    /**
     * 强制刷新请求远程数据
     * @param loadMore Boolean 加载更多
     * @param handlePullDownDown Boolean 手动下拉刷新
     */
    fun refresh(loadMore: Boolean, handlePullDownDown: Boolean = true) {
        if (isPageList()) {
            refreshListVMCompose.loadPageList(
                handlePullDownDown = handlePullDownDown,
                repoRequest = { repoPageListRequest(loadMore, true)!! })
        } else {
            refreshListVMCompose.loadList(
                handlePullDownDown = handlePullDownDown,
                repoRequest = { repoListRequest(true)!! })
        }
    }

    fun startAdd() {
        _toDetailEvent.value = true
    }

    fun delete(data: T) {
        deleteVMCompose.request(
            onSuccess = { start() },
            repoRequest = { repoDeleteRequest(data) })
    }

}