package com.boredream.koalatrace.ui.recordlist

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.PolygonOptions
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseFragment
import com.boredream.koalatrace.common.SimpleListAdapter
import com.boredream.koalatrace.common.SimpleUiStateObserver
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.TraceRecordArea
import com.boredream.koalatrace.data.constant.CommonConstant
import com.boredream.koalatrace.databinding.FragmentTraceRecordListBinding
import com.boredream.koalatrace.databinding.ItemTraceRecordBinding
import com.boredream.koalatrace.ui.trace.recorddetail.TraceRecordDetailActivity
import com.boredream.koalatrace.utils.DialogUtils
import com.boredream.koalatrace.utils.TraceUtils
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.MultiPolygon
import org.locationtech.jts.geom.Polygon
import java.util.*


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
        initView(savedInstanceState)
        initData()
        return view
    }

    private fun initData() {
        viewModel.updateAllUnFinishRecord()
        viewModel.checkUpdateRecordArea()
        viewModel.loadArea()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initView(savedInstanceState: Bundle?) {
        initDateSpinner()
        initAreaSpinner()

        // TODO: 地图模式的查询交互待优化，开始就默认查询所有的轨迹全部绘制？

        // map
        binding.mapviewList.onCreate(savedInstanceState)
        viewModel.mapListModeUiState.observe(viewLifecycleOwner) { traceList ->
            binding.mapviewList.drawMultiFixTraceList(traceList)
            val allTraceList = ArrayList<TraceLocation>()
            traceList.forEach { allTraceList.addAll(it.traceList) }
            binding.mapviewList.updateCamera2showCompleteTraceList(allTraceList)
        }

        // list
        adapter = SimpleListAdapter(dataList, R.layout.item_trace_record)
        adapter.onItemClickListener = { _, it ->
            TraceRecordDetailActivity.start(requireContext(), it)
        }
        adapter.onItemLongClickListener = { _, it ->
            DialogUtils.showDeleteConfirmDialog(requireContext(), { viewModel.delete(it) })
        }
        binding.rvList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvList.adapter = adapter
        viewModel.traceListModeUiState.observe(viewLifecycleOwner) {
            dataList.clear()
            dataList.addAll(it)
            adapter.notifyDataSetChanged()
        }
    }

    private fun initDateSpinner() {
        val menuList = arrayListOf("所有时间", "今天", "最近7天", "本月", "今年", "自定义日期")
        binding.spinnerDate.setDropMenuDataList(menuList)
        binding.spinnerDate.setOnDropMenuItemClickListener { position, data ->
            if (position == menuList.lastIndex) {
                updateByCustomRange()
            } else {
                updateByFixDateRange(data)
            }
        }
    }

    private fun updateByCustomRange() {
        // 自定义日期
        val builder = MaterialDatePicker.Builder.dateRangePicker()
        builder.setTitleText("选择日期区间")
        val dateRangePicker = builder.build()
        dateRangePicker.show(requireActivity().supportFragmentManager, "dateRangePicker")
        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startTime = selection.first
            val endTime = selection.second + CommonConstant.ONE_DAY_DURATION - 1000
            viewModel.updateDateFilter(startTime, endTime)
            val startTimeStr = TimeUtils.millis2String(startTime, "yyyy-MM-dd")
            val endTimeStr = TimeUtils.millis2String(endTime, "MM-dd")
            binding.spinnerDate.setText("$startTimeStr ~ $endTimeStr")
        }
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

    private fun initAreaSpinner() {
        SimpleUiStateObserver.setRequestObserver(
            this,
            this,
            viewModel.loadAreaVMCompose
        ) { areaList ->
            val menuList = arrayListOf("所有区域")
            if (areaList.isSuccess()) {
                menuList.addAll(areaList.getSuccessData()
                    .map { "${it.subAdminArea}-${it.locality}" })
            }
            binding.spinnerArea.setDropMenuDataList(menuList)
            binding.spinnerArea.setOnDropMenuItemClickListener { position, data ->
                var recordArea: TraceRecordArea? = null
                if (position > 0) {
                    // 一定是有数据的
                    recordArea = areaList.getSuccessData()[position - 1]
                }
                viewModel.updateAreaFilter(recordArea)
            }
        }
    }


    override fun onDestroyView() {
        binding.mapviewList.onDestroy()
        super.onDestroyView()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (isAdded) {
            if (hidden) onFragmentPause()
            else onFragmentResume()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isAdded && !isHidden) {
            onFragmentResume()
        }
    }

    private fun onFragmentResume() {
        LogUtils.v("map view onResume")
        binding.mapviewList.onResume()
        viewModel.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (isAdded && !isHidden) {
            onFragmentPause()
        }
    }

    private fun onFragmentPause() {
        LogUtils.v("map view onPause")
        binding.mapviewList.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapviewList.onSaveInstanceState(outState)
    }

}