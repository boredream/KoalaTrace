package com.boredream.koalatrace.ui.recordlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseFragment
import com.boredream.koalatrace.common.SimpleListAdapter
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.constant.CommonConstant
import com.boredream.koalatrace.databinding.FragmentTraceRecordListBinding
import com.boredream.koalatrace.databinding.ItemTraceRecordBinding
import com.boredream.koalatrace.ui.trace.recorddetail.TraceRecordDetailActivity
import com.boredream.koalatrace.utils.DialogUtils
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList


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
        val menuList = arrayListOf("所有时间", "今天", "最近7天", "本月", "今年", "自定义日期")
        binding.spinner.setDropMenuDataList(menuList)
        binding.spinner.setOnDropMenuItemClickListener {position, data ->
            if(position == menuList.lastIndex) {
                // 自定义日期
                val builder = MaterialDatePicker.Builder.dateRangePicker()
                builder.setTitleText("选择日期区间")
                val dateRangePicker = builder.build()
                dateRangePicker.show(requireActivity().supportFragmentManager, "dateRangePicker")
                dateRangePicker.addOnPositiveButtonClickListener { selection ->
                    val startTime = selection.first
                    val endTime = selection.second + CommonConstant.ONE_DAY_DURATION // 结束日期+1天，相当于当天末尾
                    viewModel.updateDateFilter(startTime, endTime)
                }
            } else {
                updateByFixDateRange(data)
            }
        }
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

    private fun updateByFixDateRange(data: String) {
        var startTime: Long? = null
        var endTime: Long? = null

        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)

        val todayEndMillis = today.timeInMillis + CommonConstant.ONE_DAY_DURATION

        when (data) {
            "今天" -> {
                startTime = today.timeInMillis
                endTime = todayEndMillis
            }
            "最近7天" -> {
                startTime = today.timeInMillis - 6 * CommonConstant.ONE_DAY_DURATION
                endTime = todayEndMillis
            }
            "本月" -> {
                val monthStart = Calendar.getInstance()
                monthStart.set(Calendar.DAY_OF_MONTH, 1)
                monthStart.set(Calendar.HOUR_OF_DAY, 0)
                monthStart.set(Calendar.MINUTE, 0)
                monthStart.set(Calendar.SECOND, 0)
                startTime = monthStart.timeInMillis
                endTime = todayEndMillis
            }
            "今年" -> {
                val monthStart = Calendar.getInstance()
                monthStart.set(Calendar.DAY_OF_YEAR, 1)
                monthStart.set(Calendar.HOUR_OF_DAY, 0)
                monthStart.set(Calendar.MINUTE, 0)
                monthStart.set(Calendar.SECOND, 0)
                startTime = monthStart.timeInMillis
                endTime = todayEndMillis
            }
        }
        viewModel.updateDateFilter(startTime, endTime)
    }

    
}