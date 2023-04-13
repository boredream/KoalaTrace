package com.boredream.koalatrace.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.CollectionUtils
import com.blankj.utilcode.util.LogUtils
import com.boredream.koalatrace.base.BaseViewModel
import com.boredream.koalatrace.common.vmcompose.RequestVMCompose
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.repo.LocationRepository
import com.boredream.koalatrace.data.usecase.TraceUseCase
import com.boredream.koalatrace.vm.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UIEvent
object LocateMe : UIEvent()

data class UiState(
    val myLocation: TraceLocation? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val traceUseCase: TraceUseCase
) : BaseViewModel() {

    val commitVMCompose = RequestVMCompose<Boolean>(viewModelScope)

    // 主UI事件
    private val _uiEvent = SingleLiveEvent<UIEvent>()
    val uiEvent: LiveData<UIEvent> = _uiEvent

    // 主UI元素
    private val _uiState = MutableLiveData(UiState())
    val uiState: LiveData<UiState> = _uiState

    private val _traceRecordUiState = MutableLiveData<TraceRecord>()
    val traceRecordUiState: LiveData<TraceRecord> = _traceRecordUiState

    // 是否显示历史轨迹
    private val _isShowHistoryTrace = MutableLiveData(false)
    val isShowHistoryTrace: LiveData<Boolean> = _isShowHistoryTrace

    private val _historyTracePointListUiState =
        MutableLiveData(ArrayList<ArrayList<TraceLocation>>())
    val historyTracePointListUiState: LiveData<ArrayList<ArrayList<TraceLocation>>> =
        _historyTracePointListUiState

    // 是否正在记录轨迹中
    private val _isTracing = MutableLiveData(false)
    val isTracing: LiveData<Boolean> = _isTracing

    /**
     * 切换显示历史轨迹
     */
    fun toggleShowHistoryTrace() {
        val old = _isShowHistoryTrace.value!!
        _isShowHistoryTrace.value = !old

        if (!old) {
            viewModelScope.launch {
                val recordList = traceUseCase.getAllHistoryTraceListRecord()
                val historyList = ArrayList<ArrayList<TraceLocation>>()
                recordList.data?.let { it ->
                    it.forEach {
                        it.traceList?.let { list -> historyList.add(list) }
                    }
                }
                _historyTracePointListUiState.value = historyList
            }
        } else {
            _historyTracePointListUiState.value = ArrayList()
        }
    }

    /**
     * 切换轨迹跟踪开关
     */
    fun toggleTrace() {
        viewModelScope.launch {
            if (traceUseCase.isTracing()) {
                traceUseCase.stopTrace()
            } else {
                traceUseCase.startTrace()
            }
        }
    }

    /**
     * 定位到我
     */
    fun locateMe() {
        _uiEvent.value = LocateMe
    }

    // 页面暂停时，无需刷新 uiState
    fun onPause() {
        traceUseCase.removeLocationSuccessListener(onLocationSuccess)
        traceUseCase.removeTraceRecordUpdateListener(onTraceRecordUpdate)
        traceUseCase.removeStatusChangeListener(onStatusChange)
    }

    fun onResume() {
        // 可能其他地方有修改，再次打开时刷新
        _isTracing.value = traceUseCase.isTracing()

        traceUseCase.addLocationSuccessListener(onLocationSuccess)
        traceUseCase.addTraceRecordUpdateListener(onTraceRecordUpdate)
        traceUseCase.addStatusChangeListener(onStatusChange)

        locateMe()
    }

    private val onLocationSuccess: (location: TraceLocation) -> Unit = {
        _uiState.value = UiState(it)
    }

    private val onTraceRecordUpdate: (traceRecord: TraceRecord) -> Unit = {
        // LogUtils.i("onTraceRecordUpdate invoked on thread: ${Thread.currentThread().name}")
        // 轨迹成功回调，回调的是整个轨迹列表。注意这里是异步线程，需要post value
        _traceRecordUiState.postValue(it)
    }

    private val onStatusChange : (status: Int) -> Unit = {
        _isTracing.value = it == LocationRepository.STATUS_TRACE
    }

}