package com.boredream.koalatrace.ui.splash

import androidx.lifecycle.viewModelScope
import com.boredream.koalatrace.base.BaseViewModel
import com.boredream.koalatrace.common.vmcompose.RequestVMCompose
import com.boredream.koalatrace.data.User
import com.boredream.koalatrace.data.repo.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(private val repository: UserRepository) :
    BaseViewModel() {

    val loginVMCompose = RequestVMCompose<User>(viewModelScope)

    fun autoLogin() {
        loginVMCompose.request { repository.autoLogin() }
    }
}
