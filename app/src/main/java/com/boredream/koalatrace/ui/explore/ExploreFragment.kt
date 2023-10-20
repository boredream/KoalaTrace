package com.boredream.koalatrace.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blankj.utilcode.util.LogUtils
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseFragment
import com.boredream.koalatrace.common.SimpleUiStateObserver
import com.boredream.koalatrace.databinding.FragmentExploreBinding
import com.boredream.koalatrace.ui.explore.ExploreViewModel
import com.boredream.koalatrace.ui.home.LocateMe
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ExploreFragment : BaseFragment<ExploreViewModel, FragmentExploreBinding>() {

    override fun getLayoutId() = R.layout.fragment_home

    override fun getViewModelClass() = ExploreViewModel::class.java

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
    }

    override fun onDestroyView() {
        LogUtils.v("map view onDestroy")
        binding.mapView.onDestroy()
        super.onDestroyView()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if(isAdded) {
            if(hidden) onFragmentPause()
            else onFragmentResume()
        }
    }

    override fun onResume() {
        super.onResume()
        if(isAdded && !isHidden) {
            onFragmentResume()
        }
    }

    private fun onFragmentResume() {
        LogUtils.v("map view onResume")
        viewModel.onResume()
        binding.mapView.setMyLocationEnable(true)
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        if(isAdded && !isHidden) {
            onFragmentPause()
        }
    }

    private fun onFragmentPause() {
        LogUtils.v("map view onPause")
        viewModel.onPause()
        binding.mapView.setMyLocationEnable(false)
        binding.mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        LogUtils.v("map view onSaveInstanceState")
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

}