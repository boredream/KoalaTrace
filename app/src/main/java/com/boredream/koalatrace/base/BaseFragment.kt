package com.boredream.koalatrace.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.ToastUtils
import com.boredream.koalatrace.BR


abstract class BaseFragment<VM: BaseViewModel, BD: ViewDataBinding>: Fragment(), BaseView {

    // base
    protected lateinit var baseActivity: BaseActivity<*, *>
    protected lateinit var viewModel: VM
    private var binding: BD? = null
    protected abstract fun getLayoutId(): Int
    protected abstract fun getViewModelClass(): Class<VM>

    fun getBinding(): BD {
        return binding!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        baseActivity = activity as BaseActivity<*, *>
        binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        viewModel = ViewModelProvider(this)[getViewModelClass()]
        getBinding().lifecycleOwner = this
        getBinding().setVariable(BR.vm, viewModel)

        viewModel.baseUiState.observe(viewLifecycleOwner) { showLoading(it.showLoading) }
        viewModel.baseEvent.observe(viewLifecycleOwner) {
            when(it) {
                is ToastLiveEvent -> ToastUtils.showShort(it.toast)
            }
        }

        return getBinding().root
    }

    override fun showLoading(show: Boolean) {
        baseActivity.showLoading(show)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}