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
    private var _binding: BD? = null
    protected val binding get() = _binding!!
    protected abstract fun getLayoutId(): Int
    protected abstract fun getViewModelClass(): Class<VM>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        baseActivity = activity as BaseActivity<*, *>
        _binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)!!
        viewModel = ViewModelProvider(this)[getViewModelClass()]
        binding.lifecycleOwner = this
        binding.setVariable(BR.vm, viewModel)

        viewModel.baseUiState.observe(viewLifecycleOwner) { showLoading(it.showLoading) }
        viewModel.baseEvent.observe(viewLifecycleOwner) {
            when(it) {
                is ToastLiveEvent -> ToastUtils.showShort(it.toast)
            }
        }

        return binding.root
    }

    override fun showLoading(show: Boolean) {
        baseActivity.showLoading(show)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}