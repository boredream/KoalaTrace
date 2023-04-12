package com.boredream.koalatrace.ui.trace.recordlist

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseFragment
import com.boredream.koalatrace.common.SimpleListAdapter
import com.boredream.koalatrace.common.SimpleUiStateObserver
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.event.SyncStatusEvent
import com.boredream.koalatrace.databinding.FragmentTraceRecordListBinding
import com.boredream.koalatrace.databinding.ItemTraceRecordBinding
import com.boredream.koalatrace.service.SyncDataService
import com.boredream.koalatrace.ui.trace.TraceMapActivity
import com.boredream.koalatrace.ui.trace.recorddetail.TraceRecordDetailActivity
import com.boredream.koalatrace.utils.DialogUtils
import com.boredream.koalatrace.utils.PermissionSettingUtil
import com.yanzhenjie.permission.AndPermission
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


@AndroidEntryPoint
class TraceRecordListFragment :
    BaseFragment<TraceRecordListViewModel, FragmentTraceRecordListBinding>() {

    override fun getLayoutId() = R.layout.fragment_trace_record_list
    override fun getViewModelClass() = TraceRecordListViewModel::class.java

    private var dataList = ArrayList<TraceRecord>()
    private lateinit var adapter: SimpleListAdapter<TraceRecord, ItemTraceRecordBinding>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        initView()
        initObserver()
        EventBus.getDefault().register(this)
        return view
    }

    override fun onResume() {
        super.onResume()
        viewModel.start()
        SyncDataService.startSync(requireContext())
    }

    override fun onDestroyView() {
        EventBus.getDefault().unregister(this)
        super.onDestroyView()
    }

    private fun initView() {
        adapter = SimpleListAdapter(dataList, R.layout.item_trace_record)
        adapter.onItemClickListener = { TraceRecordDetailActivity.start(requireContext(), it) }
        adapter.onItemLongClickListener = {
            DialogUtils.showDeleteConfirmDialog(requireContext(), { viewModel.delete(it) })
        }
        binding.refreshTraceList.setup(
            adapter,
            enableRefresh = false,
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserver() {
        binding.syncStatusView.setOnClickListener {
            SyncDataService.startSync(requireContext())
        }
        viewModel.toDetailEvent.observe(viewLifecycleOwner) { toDetail() }
        SimpleUiStateObserver.setRequestObserver(this, this, viewModel.deleteVMCompose) {
            // 提交成功后，开始推送信息
            SyncDataService.startPush(requireContext())
        }
    }

    //接收消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSyncStatusEvent(event: SyncStatusEvent) {
        viewModel.setSyncStatus(event.isSyncing)
        if(!event.isSyncing) {
            // 刷新完成后，更新UI
            viewModel.start()
        }
    }

    private fun toDetail() {
        AndPermission.with(context)
            .runtime()
            .permission(Manifest.permission.ACCESS_FINE_LOCATION)
            .onGranted {
                TraceMapActivity.start(requireContext())
            }
            .onDenied { permissions ->
                if (AndPermission.hasAlwaysDeniedPermission(context, permissions)) {
                    PermissionSettingUtil.showSetting(requireContext(), permissions)
                }
            }
            .start()
    }
}