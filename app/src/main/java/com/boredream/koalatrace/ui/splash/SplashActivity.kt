package com.boredream.koalatrace.ui.splash

import android.Manifest
import android.os.Bundle
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseActivity
import com.boredream.koalatrace.databinding.ActivitySplashBinding
import com.boredream.koalatrace.ui.login.LoginActivity
import com.boredream.koalatrace.ui.main.MainTabActivity
import com.boredream.koalatrace.utils.PermissionUtil
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SplashActivity : BaseActivity<SplashViewModel, ActivitySplashBinding>() {

    override fun getLayoutId() = R.layout.activity_splash
    override fun getViewModelClass() = SplashViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.loginVMCompose.successUiState.observe(this) {
            MainTabActivity.start(this)
            finish()
        }
        viewModel.loginVMCompose.failUiState.observe(this) {
            LoginActivity.start(this)
            finish()
        }

        PermissionUtil.request(this, listOf(Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (it) init() else finish()
        }
    }

    private fun init() {
        viewModel.autoLogin()
    }

}