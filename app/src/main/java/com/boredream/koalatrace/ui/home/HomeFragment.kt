package com.boredream.koalatrace.ui.home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startForegroundService
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ServiceUtils.startService
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseFragment
import com.boredream.koalatrace.common.SimpleUiStateObserver
import com.boredream.koalatrace.data.constant.BundleKey
import com.boredream.koalatrace.databinding.FragmentHomeBinding
import com.boredream.koalatrace.service.SyncDataService
import com.boredream.koalatrace.service.TraceLocationService
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>() {

    private lateinit var serviceIntent: Intent

    override fun getLayoutId() = R.layout.fragment_home

    override fun getViewModelClass() = HomeViewModel::class.java

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)
        initObserver()
        toggleLocation(true)
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

    private fun toggleLocation(start: Boolean) {
        // TODO: start service 应该放在架构哪一层？
        serviceIntent = Intent(activity, TraceLocationService::class.java)
        serviceIntent.putExtra(BundleKey.TOGGLE_LOCATION, start)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(requireContext(), serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    override fun onDestroy() {
        toggleLocation(false)
        binding.mapView.onDestroy()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        viewModel.onPause()
        binding.mapView.onPause()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        try {
            // FIXME: binding NPE?
            binding.mapView.onSaveInstanceState(outState)
        } catch (e: Exception) {
            e.printStackTrace()
            LogUtils.i("error", e.message)
        }
    }

}