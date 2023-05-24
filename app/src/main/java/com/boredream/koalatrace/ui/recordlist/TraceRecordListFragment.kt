package com.boredream.koalatrace.ui.recordlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amap.api.mapcore.util.it
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseFragment
import com.boredream.koalatrace.common.SimpleListAdapter
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.databinding.FragmentTraceRecordListBinding
import com.boredream.koalatrace.databinding.ItemTraceRecordBinding
import com.boredream.koalatrace.ui.trace.recorddetail.TraceRecordDetailActivity
import com.boredream.koalatrace.utils.DialogUtils
import dagger.hilt.android.AndroidEntryPoint


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
        viewModel.updateAllUnFinishRecord()
        viewModel.checkUpdateRecordArea()
        return view
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    private fun initView() {
        adapter = SimpleListAdapter(dataList, R.layout.item_trace_record)
        adapter.onItemClickListener = { _, it ->
            TraceRecordDetailActivity.start(requireContext(), it)
        }
        adapter.onItemLongClickListener = { _, it ->
            DialogUtils.showDeleteConfirmDialog(requireContext(), { viewModel.delete(it) })
        }
        binding.refreshTraceList.setup(
            adapter,
            enableRefresh = false,
        )
    }
}