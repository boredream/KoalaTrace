package com.boredream.koalatrace.ui.recordlist

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
import com.boredream.koalatrace.ui.trace.recorddetail.TraceRecordDetailActivity
import com.boredream.koalatrace.utils.DialogUtils
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
        viewModel.updateRecordingTraces()
        EventBus.getDefault().register(this)
        return view
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
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
        SimpleUiStateObserver.setRequestObserver(this, this, viewModel.deleteVMCompose) {
            // 提交成功后，开始推送信息
            SyncDataService.startPush(requireContext())
        }
    }

    // 接收消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSyncStatusEvent(event: SyncStatusEvent) {
        // TODO: 这种方式好吗？ service 和 activity 直接通信，可以用event；但是如果都引用相同的repo，是否用回调更好？
        viewModel.setSyncStatus(event.isSyncing)
        if(!event.isSyncing) {
            // 刷新完成后，更新UI
            viewModel.loadData()
        }
    }
}