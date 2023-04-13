package com.boredream.koalatrace.ui.recordlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.boredream.koalatrace.base.BaseViewModel
import com.boredream.koalatrace.common.vmcompose.RefreshListVMCompose
import com.boredream.koalatrace.common.vmcompose.RequestVMCompose
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.repo.TraceRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TraceRecordListViewModel @Inject constructor(
    private val repository: TraceRecordRepository,
) : BaseViewModel() {

    val refreshListVMCompose = RefreshListVMCompose(viewModelScope)
    val deleteVMCompose = RequestVMCompose<TraceRecord>(viewModelScope)

    private val _isSyncingState = MutableLiveData(false)
    val isSyncingState: LiveData<Boolean> = _isSyncingState

    fun start() {
        refreshListVMCompose.loadList { repository.getList() }
    }

    fun setSyncStatus(isSyncing: Boolean) {
        _isSyncingState.value = isSyncing
    }

    fun delete(data: TraceRecord) {
        deleteVMCompose.request(
            onSuccess = { start() }
        ) { repository.delete(data) }
    }

    fun updateRecordingTraces() {
        // 如果有之前未保存的，刷新轨迹数据
        viewModelScope.launch {
            val hasUpdate = repository.checkAllRecordUpdateByTraceList()
            if(hasUpdate) {
                start()
            }
        }
    }

}