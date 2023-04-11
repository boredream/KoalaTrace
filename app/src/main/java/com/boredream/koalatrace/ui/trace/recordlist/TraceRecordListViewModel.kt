package com.boredream.koalatrace.ui.trace.recordlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.boredream.koalatrace.base.BaseViewModel
import com.boredream.koalatrace.common.vmcompose.RefreshListVMCompose
import com.boredream.koalatrace.common.vmcompose.RequestVMCompose
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.repo.TraceRecordRepository
import com.boredream.koalatrace.vm.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TraceRecordListViewModel @Inject constructor(
    private val repository: TraceRecordRepository,
) : BaseViewModel() {

    val refreshListVMCompose = RefreshListVMCompose(viewModelScope)
    val deleteVMCompose = RequestVMCompose<TraceRecord>(viewModelScope)

    private val _toDetailEvent = SingleLiveEvent<Boolean>()
    val toDetailEvent: LiveData<Boolean> = _toDetailEvent

    private val _isSyncingState = MutableLiveData(false)
    val isSyncingState: LiveData<Boolean> = _isSyncingState

    fun start() {
        refreshListVMCompose.loadList { repository.getList() }
    }

    fun setSyncStatus(isSyncing: Boolean) {
        _isSyncingState.value = isSyncing
    }

    fun startAdd() {
        _toDetailEvent.value = true
    }

    fun delete(data: TraceRecord) {
        deleteVMCompose.request(
            onSuccess = { start() }
        ) { repository.delete(data) }
    }

}