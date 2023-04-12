package com.boredream.koalatrace.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.blankj.utilcode.util.ToastUtils
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseActivity
import com.boredream.koalatrace.base.RepoCacheHelper
import com.boredream.koalatrace.common.SimpleUiStateObserver
import com.boredream.koalatrace.databinding.ActivityLoginBinding
import com.boredream.koalatrace.ui.main.MainTabActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LoginActivity : BaseActivity<LoginViewModel, ActivityLoginBinding>() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getLayoutId() = R.layout.activity_login

    override fun getViewModelClass() = LoginViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 登录时，清空原有所有缓存
        RepoCacheHelper.repoCache.clear()

        SimpleUiStateObserver.setRequestObserver(this, this, viewModel.loginVMCompose) {
            ToastUtils.showShort("登录成功")
            MainTabActivity.start(this)
            finish()
        }
    }

}