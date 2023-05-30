package com.boredream.koalatrace.ui.recordlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.boredream.koalatrace.base.BaseViewModel
import com.boredream.koalatrace.base.ToastLiveEvent
import com.boredream.koalatrace.common.vmcompose.RequestVMCompose
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.TraceRecordArea
import com.boredream.koalatrace.data.repo.TraceRecordRepository
import com.boredream.koalatrace.data.usecase.TraceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TraceRecordListViewModel @Inject constructor(
    private val userCase: TraceUseCase,
    private val repository: TraceRecordRepository,
) : BaseViewModel() {

    val loadListVMCompose = RequestVMCompose<ArrayList<TraceRecord>>(viewModelScope)
    val deleteVMCompose = RequestVMCompose<TraceRecord>(viewModelScope)
    val loadAreaVMCompose = RequestVMCompose<ArrayList<TraceRecordArea>>(viewModelScope)

    private val _traceListModeUiState = MutableLiveData<ArrayList<TraceRecord>>()
    val traceListModeUiState: LiveData<ArrayList<TraceRecord>> = _traceListModeUiState

    private val _mapListModeUiState = MutableLiveData<ArrayList<TraceRecord>>()
    val mapListModeUiState: LiveData<ArrayList<TraceRecord>> = _mapListModeUiState

    private val _isListModeUiState = MutableLiveData(true)
    val isListModeUiState: LiveData<Boolean> = _isListModeUiState

    // 条件
    private var startTime : Long? = null
    private var endTime : Long? = null
    private var recordArea : TraceRecordArea? = null

    fun onResume() {
        loadData()
        // do other things
    }

    fun updateDateFilter(startTime: Long?, endTime: Long?) {
        this.startTime = startTime
        this.endTime = endTime
        loadData()
    }

    fun updateAreaFilter(recordArea: TraceRecordArea?) {
        this.recordArea = recordArea
        loadData()
    }

    fun loadData() {
        loadListVMCompose.request(
            onSuccess = { onLoadTraceListSuccess(it) } // 数据回来之后，刷新列表
        ) {
            val isListMode = isListModeUiState.value ?: true
            if(isListMode) {
                repository.getListByCondition(startTime, endTime, recordArea)
            } else {
                // TODO: 速度？
                val response = repository.getListByCondition(startTime, endTime, recordArea)
                if(response.isSuccess()) {
                    response.getSuccessData().forEach {
                        val listResponse = repository.getLocationList(it.id)
                        if(listResponse.isSuccess()) {
                            it.traceList = listResponse.getSuccessData()
                        }
                    }
                }
                response
            }
        }
    }

    private fun onLoadTraceListSuccess(traceList: ArrayList<TraceRecord>) {
        val isListMode = isListModeUiState.value ?: true
        if(isListMode) {
            _traceListModeUiState.value = traceList
        } else {
            _mapListModeUiState.value = traceList
        }
    }

    fun toggleListMode() {
        if(recordArea == null) {
            _baseEvent.value = ToastLiveEvent("只能在xx市xx区的指定区域下，才能切换地图模式")
            return
        }

        val isListMode = isListModeUiState.value ?: true
        _isListModeUiState.value = !isListMode

        loadData()
    }

    fun delete(data: TraceRecord) {
        deleteVMCompose.request(
            onSuccess = { loadData() }
        ) { repository.delete(data) }
    }

    fun updateAllUnFinishRecord() {
        // 如果有之前未保存的，刷新轨迹数据
        viewModelScope.launch {
            val hasUpdate = userCase.refreshUnFinishTrace()
            if(hasUpdate) {
                loadData()
            }
        }
    }

    fun checkUpdateRecordArea() {
        // 如果有之前未保存的，刷新轨迹数据
        viewModelScope.launch {
            val hasUpdate = userCase.checkUpdateRecordArea()
            if(hasUpdate) {
                loadData()
            }
        }
    }

    fun loadArea() {
        loadAreaVMCompose.request {
            repository.loadArea()
        }
    }
}