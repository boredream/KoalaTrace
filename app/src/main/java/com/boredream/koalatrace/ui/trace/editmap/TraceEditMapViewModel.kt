package com.boredream.koalatrace.ui.trace.editmap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.boredream.koalatrace.base.BaseViewModel
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.repo.TraceRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TraceEditMapViewModel @Inject constructor(
    private val repository: TraceRecordRepository
) : BaseViewModel() {

    private var step = 1800
    private lateinit var data: TraceRecord
    private var traceList = ArrayList<TraceLocation>()

    private val _traceListUiState = MutableLiveData(ArrayList<ArrayList<TraceLocation>>())
    val traceListUiState: LiveData<ArrayList<ArrayList<TraceLocation>>> = _traceListUiState

    private val _startLocationUiState = MutableLiveData<TraceLocation>()
    val startLocationUiState: LiveData<TraceLocation> = _startLocationUiState

    /**
     * 页面开始时，绘制路线，并跳转到对应位置
     */
    fun start(data: TraceRecord) {
        this.data = data

        // TODO:  根据路线，选择合适的 camera zoom 和 position
        data.traceList?.let { _startLocationUiState.value = it[step] }
    }

    fun preStepTrace() {
        traceList.removeLast()
        _traceListUiState.value = arrayListOf(traceList)
        step -= 1
    }

    fun nextStepTrace() {
//        val newTracePoint = data.traceList?.get(step)
//        traceList.add(newTracePoint)
//        _traceListUiState.value = arrayListOf(traceList)
//        step += 1
    }

}