package com.boredream.koalatrace.ui.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.boredream.koalatrace.base.BaseViewModel
import com.boredream.koalatrace.common.vmcompose.RequestVMCompose
import com.boredream.koalatrace.data.ExploreAreaInfo
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.TraceRecordArea
import com.boredream.koalatrace.data.constant.MapConstant
import com.boredream.koalatrace.data.usecase.ExploreUseCase
import com.boredream.koalatrace.ui.home.UIEvent
import com.boredream.koalatrace.vm.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.exp

data class UiState(
    val totalArea: String,
    val totalBlockCount: Int,
    val exploreBlockCount: Int,
    val lightBlockCount: Int,
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val useCase: ExploreUseCase
) : BaseViewModel() {

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState

    val loadAreaVMCompose = RequestVMCompose<ArrayList<TraceRecordArea>>(viewModelScope)
    val loadVMCompose = RequestVMCompose<ExploreAreaInfo>(viewModelScope)

    fun init() {
        loadAreaVMCompose.request {
            useCase.loadArea()
        }
    }

    fun drawExplore(area: TraceRecordArea) {
        loadVMCompose.request(onSuccess = { areaInfo ->
            var exploreBlockCount = 0
            var lightBlockCount = 0
            areaInfo.blockList.forEach {
                if(it.explorePercent > 0) exploreBlockCount ++
                if(it.explorePercent > MapConstant.EXPLORE_LIGHT_RATIO_THRESHOLD) lightBlockCount ++
            }
            _uiState.value = UiState(areaInfo.areaCode, areaInfo.blockList.size, exploreBlockCount, lightBlockCount)
        }) {
            useCase.calculateAreaExplore(area)
        }
    }

}