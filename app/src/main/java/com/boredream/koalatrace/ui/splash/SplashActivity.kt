package com.boredream.koalatrace.ui.splash

import android.Manifest
import android.os.Bundle
import androidx.core.content.ContentProviderCompat.requireContext
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseActivity
import com.boredream.koalatrace.databinding.ActivitySplashBinding
import com.boredream.koalatrace.ui.login.LoginActivity
import com.boredream.koalatrace.ui.main.MainTabActivity
import com.boredream.koalatrace.ui.trace.recorddetail.TraceRecordDetailActivity
import com.boredream.koalatrace.utils.PermissionSettingUtil
import com.yanzhenjie.permission.AndPermission
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

        AndPermission.with(this)
            .runtime()
            .permission(Manifest.permission.ACCESS_FINE_LOCATION)
            .onGranted {
                init()
            }
            .onDenied { permissions ->
                if (AndPermission.hasAlwaysDeniedPermission(this, permissions)) {
                    PermissionSettingUtil.showSetting(this, permissions)
                }
            }
            .start()
    }

    private fun init() {
        viewModel.autoLogin()
    }

}