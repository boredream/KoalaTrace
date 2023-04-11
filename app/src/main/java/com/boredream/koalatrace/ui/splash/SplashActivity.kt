package com.boredream.koalatrace.ui.splash

import android.os.Bundle
import com.boredream.koalatrace.ui.MainActivity
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseActivity
import com.boredream.koalatrace.databinding.ActivitySplashBinding
import com.boredream.koalatrace.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SplashActivity : BaseActivity<SplashViewModel, ActivitySplashBinding>() {

    // TODO: The application should not provide its own launch screen

    override fun getLayoutId() = R.layout.activity_splash

    override fun getViewModelClass() = SplashViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.loginVMCompose.successUiState.observe(this) {
            MainActivity.start(this)
            finish()
        }
        viewModel.loginVMCompose.failUiState.observe(this) {
            LoginActivity.start(this)
            finish()
        }

        viewModel.autoLogin()
    }

}