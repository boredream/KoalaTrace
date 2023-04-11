package com.boredream.koalatrace.ui.mine

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.boredream.koalatrace.base.BaseViewModel
import com.boredream.koalatrace.data.User
import com.boredream.koalatrace.data.repo.UserRepository
import com.boredream.koalatrace.vm.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MineViewModel @Inject constructor(private val repository: UserRepository) : BaseViewModel() {

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

}

sealed class MineEventState
object LogoutEvent : MineEventState()
