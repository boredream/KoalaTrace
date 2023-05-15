package com.boredream.koalatrace.ui.recordlist

import androidx.lifecycle.viewModelScope
import com.boredream.koalatrace.base.BaseViewModel
import com.boredream.koalatrace.common.vmcompose.RefreshListVMCompose
import com.boredream.koalatrace.common.vmcompose.RequestVMCompose
import com.boredream.koalatrace.data.TraceRecord
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

    val refreshListVMCompose = RefreshListVMCompose(viewModelScope)
    val deleteVMCompose = RequestVMCompose<TraceRecord>(viewModelScope)

    fun onResume() {
        loadData()
        // do other things
    }

    fun loadData() {
        refreshListVMCompose.loadList { repository.getList() }
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

}