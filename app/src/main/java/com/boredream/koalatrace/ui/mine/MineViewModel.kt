package com.boredream.koalatrace.ui.mine

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.TimeUtils
import com.boredream.koalatrace.base.BaseViewModel
import com.boredream.koalatrace.base.ShowConfirmDialogLiveEvent
import com.boredream.koalatrace.base.ToastLiveEvent
import com.boredream.koalatrace.data.repo.BackupRepository
import com.boredream.koalatrace.vm.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MineViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
) : BaseViewModel() {

    private val _eventUiState = SingleLiveEvent<MineEventState>()
    val eventUiState: LiveData<MineEventState> = _eventUiState

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
        val fileSize = FileUtils.getSize(filePath)
        val fileLastModifyTime =
            TimeUtils.millis2String(FileUtils.getFileLastModified(backupRepository.backupDbFile))
        val content = StringBuilder()
        content.append("备份文件位置：\n").append(filePath).append("\n\n")
            .append("备份文件大小：\n").append(fileSize).append("\n\n")
            .append("上次备份时间：\n").append(fileLastModifyTime).append("\n\n")
            .append("恢复操作会把当前应用已有数据全部覆盖，之后重新启动应用生效，请谨慎操作！")
        _baseEvent.value = ShowConfirmDialogLiveEvent(title, content.toString(), "确认恢复") {
            restoreDB()
        }
    }

    private fun restoreDB() {
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
object RestoreSuccessEvent : MineEventState()
