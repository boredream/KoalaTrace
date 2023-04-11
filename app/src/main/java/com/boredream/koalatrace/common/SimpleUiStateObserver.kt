package com.boredream.koalatrace.common

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.blankj.utilcode.util.ToastUtils
import com.boredream.koalatrace.base.BaseView
import com.boredream.koalatrace.common.vmcompose.RequestVMCompose
import com.boredream.koalatrace.data.ResponseEntity

/**
 * 简易UiState观察类，连接 BaseRequestViewModel 和 常用 UI
 */
object SimpleUiStateObserver {

    /**
     * 设置请求观察者。
     * 参数为默认方法，需要替换操作时，传入参数；需要追加操作时，在外部再次自行observe
     */
    fun <T> setRequestObserver(
        baseView: BaseView,
        lifecycleOwner: LifecycleOwner,
        requestVMCompose: RequestVMCompose<T>,
        isRequestingObserver: Observer<Boolean> = Observer<Boolean> {
            // 默认发起和结束请求时，更新loading
            baseView.showLoading(it)
        },
        failObserver: Observer<ResponseEntity<T>> = Observer<ResponseEntity<T>> {
            // 默认请求失败时，Toast
            ToastUtils.showLong("请求失败 ${it.msg}")
        },
        successObserver: Observer<ResponseEntity<T>> = Observer<ResponseEntity<T>> {
            // 默认请求成功时，Toast
            // FIXME:  Activity com.boredream.koalatrace.ui.trace.TraceMapActivity has leaked window
            // ToastUtils.showLong("请求成功")
        }
    ) {
        requestVMCompose.isRequestingUiState.observe(lifecycleOwner, isRequestingObserver)
        requestVMCompose.successUiState.observe(lifecycleOwner, successObserver)
        requestVMCompose.failUiState.observe(lifecycleOwner, failObserver)
    }

}

