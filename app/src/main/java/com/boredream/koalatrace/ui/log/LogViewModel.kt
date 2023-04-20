package com.boredream.koalatrace.ui.log

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import com.boredream.koalatrace.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor() : BaseViewModel() {

    private val _uiState = MutableLiveData<ArrayList<File>>()
    val uiState: LiveData<ArrayList<File>> = _uiState

    fun start() {
        val fileList = FileUtils.listFilesInDir(LogUtils.getConfig().dir)
        _uiState.value = ArrayList(fileList)
    }

}