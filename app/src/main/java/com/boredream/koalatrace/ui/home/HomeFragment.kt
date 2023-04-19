package com.boredream.koalatrace.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blankj.utilcode.util.LogUtils
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseFragment
import com.boredream.koalatrace.common.SimpleUiStateObserver
import com.boredream.koalatrace.databinding.FragmentHomeBinding
import com.boredream.koalatrace.service.SyncDataService
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>() {

    override fun getLayoutId() = R.layout.fragment_home

    override fun getViewModelClass() = HomeViewModel::class.java

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        LogUtils.v("map view onCreate")
        binding.mapView.onCreate(savedInstanceState)
        initObserver()
        return view
    }

    private fun initObserver() {
        viewModel.uiEvent.observe(viewLifecycleOwner) {
            when (it) {
                is LocateMe -> binding.mapView.apply {
                    post { locateMe() }
                }
            }
        }

        SimpleUiStateObserver.setRequestObserver(this, this, viewModel.commitVMCompose)
        viewModel.commitVMCompose.successUiState.observe(viewLifecycleOwner) {
            // 提交成功后，开始推送信息
            SyncDataService.startPush(requireContext())
        }
    }

    override fun onDestroyView() {
        LogUtils.v("map view onDestroy")
        binding.mapView.onDestroy()
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        LogUtils.v("map view onResume")
        viewModel.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        LogUtils.v("map view onPause")
        viewModel.onPause()
        binding.mapView.onPause()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        LogUtils.v("map view onSaveInstanceState")
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

}