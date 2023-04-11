package com.boredream.koalatrace.ui.home

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startForegroundService
import com.blankj.utilcode.util.ServiceUtils.startService
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseFragment
import com.boredream.koalatrace.common.SimpleUiStateObserver
import com.boredream.koalatrace.data.constant.BundleKey
import com.boredream.koalatrace.databinding.FragmentHomeBinding
import com.boredream.koalatrace.service.SyncDataService
import com.boredream.koalatrace.service.TraceLocationService
import com.boredream.koalatrace.ui.trace.TraceMapActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>() {

    private lateinit var serviceIntent: Intent

    override fun getLayoutId() = R.layout.fragment_home

    override fun getViewModelClass() = HomeViewModel::class.java

    companion object {

        fun start(context: Context) {
            val intent = Intent(context, TraceMapActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        getBinding().mapView.onCreate(savedInstanceState)
        initObserver()
        toggleLocation(true)
        return view
    }

    private fun initObserver() {
        viewModel.uiEvent.observe(viewLifecycleOwner) {
            when (it) {
                is ShowSaveConfirmDialog -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle("提醒")
                        .setMessage("是否保存当前轨迹？")
                        .setPositiveButton("保存") { _, _ -> viewModel.saveTrace() }
                        .setNegativeButton("删除") { _, _ -> viewModel.abandonTrace() }
                        .show()
                }
                is LocateMe -> getBinding().mapView.apply {
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
        getBinding().mapView.onDestroy()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
        getBinding().mapView.onResume()
    }

    override fun onPause() {
        viewModel.onPause()
        getBinding().mapView.onPause()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        getBinding().mapView.onSaveInstanceState(outState)
    }

}