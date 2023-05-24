package com.boredream.koalatrace.ui.trace.recorddetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amap.api.mapcore.util.it
import com.blankj.utilcode.util.StringUtils
import com.boredream.koalatrace.base.BaseViewModel
import com.boredream.koalatrace.base.ToastLiveEvent
import com.boredream.koalatrace.common.vmcompose.RequestVMCompose
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.usecase.TraceDetailUseCase
import com.boredream.koalatrace.utils.Logger
import com.boredream.koalatrace.utils.TraceUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TraceRecordDetailViewModel @Inject constructor(
    private val logger: Logger,
    private val useCase: TraceDetailUseCase
) : BaseViewModel() {

    val commitVMCompose = RequestVMCompose<TraceRecord>(viewModelScope)
    val requestVMCompose = RequestVMCompose<ArrayList<TraceLocation>>(viewModelScope)
    val deleteLocationVMCompose = RequestVMCompose<TraceRecord>(viewModelScope)

    private val _uiState = MutableLiveData<TraceRecord>()
    val uiState: LiveData<TraceRecord> = _uiState

    private val _selectLocationState = MutableLiveData<TraceLocation>()
    val selectLocationState: LiveData<TraceLocation> = _selectLocationState

    private lateinit var data: TraceRecord

    /**
     * 页面开始时，绘制路线，并跳转到对应位置
     */
    fun start(data: TraceRecord) {
        this.data = data
        useCase.init(data.id)
        requestVMCompose.request(
            onSuccess = {
                data.traceList = it
                _uiState.value = data
            },
            repoRequest = { useCase.getTraceList() }
        )
    }

    fun deleteTraceLocationAt(position: Int) {
        deleteLocationVMCompose.request(
            onSuccess = { _uiState.value = data },
            repoRequest = { useCase.deleteTraceLocation(data, position) })
    }

    fun commit() {
        val data = _uiState.value!!
        if (StringUtils.isEmpty(data.name)) {
            _baseEvent.value = ToastLiveEvent("名字不能为空")
            return
        }
        commitVMCompose.request { useCase.updateRecord(data) }
    }

    fun selectLocation(location: TraceLocation) {
        _selectLocationState.value = location
    }

    fun selectMostNearlyLocation(latitude: Double, longitude: Double, zoomLevel: Float) {
        val location = TraceUtils.getMostNearlyLocation(data.traceList, latitude, longitude, zoomLevel)
        location?.let { selectLocation(it) }
    }


}