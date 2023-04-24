package com.boredream.koalatrace.ui.mine

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.ToastUtils
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseViewModel
import com.boredream.koalatrace.base.ToastLiveEvent
import com.boredream.koalatrace.data.User
import com.boredream.koalatrace.data.constant.CommonConstant
import com.boredream.koalatrace.data.repo.BackupRepository
import com.boredream.koalatrace.data.repo.UserRepository
import com.boredream.koalatrace.vm.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@HiltViewModel
class MineViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
    private val repository: UserRepository,
    ) : BaseViewModel() {

    private val _uiState = MutableLiveData<User>()
    val uiState: LiveData<User> = _uiState

    private val _eventUiState = SingleLiveEvent<MineEventState>()
    val eventUiState: LiveData<MineEventState> = _eventUiState

    fun loadUserInfo() {
        // 直接从本地取
        val user = repository.getLocalUser() ?: return
        _uiState.value = user
    }

    fun logout() {
        repository.logout()
        _eventUiState.value = LogoutEvent
    }

    fun backupDB() {
        viewModelScope.launch {
            val response = backupRepository.backup()
            if (!response.isSuccess()) {
                _baseEvent.value = ToastLiveEvent("备份失败：" + response.msg)
            } else {
                _baseEvent.value = ToastLiveEvent(response.getSuccessData())
            }
        }
    }

    fun intent2RestoreDB() {
        backupRepository
    }

    fun restoreDB() {
        val response = backupRepository.restore()
        if (!response.isSuccess()) {
            _baseEvent.value = ToastLiveEvent("恢复备份失败：" + response.msg)
        } else {
            _baseEvent.value = ToastLiveEvent(response.getSuccessData())
            _eventUiState.value = RestoreSuccessEvent
        }
    }

}

sealed class MineEventState
object LogoutEvent : MineEventState()
object RestoreSuccessEvent : MineEventState()
