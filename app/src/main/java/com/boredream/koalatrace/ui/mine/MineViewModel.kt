package com.boredream.koalatrace.ui.mine

import android.text.format.DateUtils
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.TimeUtils
import com.boredream.koalatrace.base.BaseViewModel
import com.boredream.koalatrace.base.ShowConfirmDialogLiveEvent
import com.boredream.koalatrace.base.ToastLiveEvent
import com.boredream.koalatrace.data.User
import com.boredream.koalatrace.data.repo.BackupRepository
import com.boredream.koalatrace.data.repo.UserRepository
import com.boredream.koalatrace.vm.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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

    fun showRestoreDbConfirmDialog() {
        val title = "恢复备份提醒"
        val filePath = backupRepository.backupDbFile.absolutePath
        val fileLastModifyTime = TimeUtils.millis2String(FileUtils.getFileLastModified(backupRepository.backupDbFile))
        val content = StringBuilder()
        // TODO: 文件大小
        content.append("备份文件位置：\n").append(filePath).append("\n\n")
            .append("上次备份时间：\n").append(fileLastModifyTime).append("\n\n")
            .append("恢复操作会把当前应用已有数据全部覆盖，之后重新启动应用生效，请谨慎操作！")
        _baseEvent.value = ShowConfirmDialogLiveEvent(title, content.toString(), "确认恢复") {
            restoreDB()
        }
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
