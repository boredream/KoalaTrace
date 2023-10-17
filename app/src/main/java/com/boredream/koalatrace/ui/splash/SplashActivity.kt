package com.boredream.koalatrace.ui.splash

import android.Manifest
import android.os.Bundle
import com.amap.api.location.AMapLocationClient
import com.amap.api.maps.MapsInitializer
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseActivity
import com.boredream.koalatrace.databinding.ActivitySplashBinding
import com.boredream.koalatrace.ui.main.MainTabActivity
import com.boredream.koalatrace.utils.PermissionUtil
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SplashActivity : BaseActivity<SplashViewModel, ActivitySplashBinding>() {

    override fun getLayoutId() = R.layout.activity_splash
    override fun getViewModelClass() = SplashViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.uiState.observe(this) {
            MainTabActivity.start(this)
            finish()
        }

        AMapLocationClient.updatePrivacyShow(this, true, true)
        AMapLocationClient.updatePrivacyAgree(this, true)
        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)

        PermissionUtil.request(this, listOf(Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (it) init() else finish()
        }
    }

    private fun init() {
        viewModel.autoLogin()
    }

}