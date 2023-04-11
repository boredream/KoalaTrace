package com.boredream.koalatrace.base

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.ToastUtils
import com.boredream.koalatrace.BR


abstract class BaseActivity<VM : BaseViewModel, BD : ViewDataBinding> : AppCompatActivity(),
    BaseView {

    // base
    lateinit var viewModel: VM
    lateinit var binding: BD
    abstract fun getLayoutId(): Int
    abstract fun getViewModelClass(): Class<VM>

    // view
    private lateinit var loadingDialog: ProgressDialog

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding = DataBindingUtil.setContentView(this, getLayoutId())
        viewModel = ViewModelProvider(this)[getViewModelClass()]
        binding.lifecycleOwner = this
        binding.setVariable(BR.vm, viewModel)

        loadingDialog = ProgressDialog(this)
        loadingDialog.setMessage("加载中...")

        viewModel.baseUiState.observe(this) { showLoading(it.showLoading) }
        viewModel.baseEvent.observe(this) {
            when(it) {
                is ToastLiveEvent -> ToastUtils.showShort(it.toast)
            }
        }
    }

    override fun showLoading(show: Boolean) {
        if (show) loadingDialog.show()
        else loadingDialog.dismiss()
    }

}