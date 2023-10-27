package com.boredream.koalatrace.ui.explore

import androidx.lifecycle.viewModelScope
import com.boredream.koalatrace.base.BaseViewModel
import com.boredream.koalatrace.common.vmcompose.RequestVMCompose
import com.boredream.koalatrace.data.ExploreAreaInfo
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.TraceRecordArea
import com.boredream.koalatrace.data.usecase.ExploreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val useCase: ExploreUseCase
) : BaseViewModel() {

    val loadAreaVMCompose = RequestVMCompose<ArrayList<TraceRecordArea>>(viewModelScope)
    val loadVMCompose = RequestVMCompose<ExploreAreaInfo>(viewModelScope)

    fun init() {
        loadAreaVMCompose.request {
            useCase.loadArea()
        }
    }

    fun drawExplore(area: TraceRecordArea) {
        loadVMCompose.request {
            useCase.calculateAreaExplore(area)
        }
    }

}