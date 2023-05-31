package com.boredream.koalatrace.ui.trace.recorddetail

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseActivity
import com.boredream.koalatrace.common.SimpleUiStateObserver
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.constant.BundleKey
import com.boredream.koalatrace.databinding.ActivityTraceRecordDetailBinding
import com.boredream.koalatrace.utils.DialogUtils
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TraceRecordDetailActivity :
    BaseActivity<TraceRecordDetailViewModel, ActivityTraceRecordDetailBinding>() {

    private lateinit var data: TraceRecord
    private lateinit var adapter: TraceLocationAdapter

    override fun getLayoutId() = R.layout.activity_trace_record_detail

    override fun getViewModelClass() = TraceRecordDetailViewModel::class.java

    companion object {

        fun start(context: Context, data: TraceRecord) {
            LogUtils.i("show TraceRecordDetail $data")
            val intent = Intent(context, TraceRecordDetailActivity::class.java)
            intent.putExtra(BundleKey.DATA, data)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        data = intent.extras?.getSerializable(BundleKey.DATA) as TraceRecord
        initView(savedInstanceState)
        initObserver()
        viewModel.start(data)
    }

    private fun initView(savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
        binding.rvLocationList.layoutManager = LinearLayoutManager(this)
        binding.mapView.onMapClickListener = {
            viewModel.selectMostNearlyLocation(it.latitude, it.longitude, binding.mapView.zoomLevel)
        }
    }

    private fun initObserver() {
        viewModel.uiState.observe(this) { onGetRecordSuccess() }
        viewModel.selectLocationState.observe(this) {
            // TODO: 性能？
            val index = data.traceList.indexOf(it)
            binding.rvLocationList.layoutManager?.scrollToPosition(index)
            onItemClick(index)
        }
        SimpleUiStateObserver.setRequestObserver(
            this,
            this,
            viewModel.commitVMCompose
        ) {
            ToastUtils.showShort("修改成功")
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onGetRecordSuccess() {
        // TODO: 删除应该局部刷新？
        if (binding.rvLocationList.adapter == null) {
            adapter = TraceLocationAdapter(data.traceList)
            adapter.onItemClickListener = { position, _ ->
                onItemClick(position)
            }
            adapter.onItemLongClickListener = { position, _ ->
                DialogUtils.showDeleteConfirmDialog(this, {
                    viewModel.deleteTraceLocationAt(position)
                })
            }
            binding.rvLocationList.adapter = adapter
            binding.mapView.updateCamera2showCompleteTraceList(data.traceList)
        } else {
            binding.mapView.clearLineList()
            adapter.notifyDataSetChanged()
        }
        binding.mapView.drawFixTraceList(data.traceList)
    }

    private fun onItemClick(position: Int) {
        // 更新列表选中
        if (adapter.selectedPosition != -1) {
            // old
            adapter.notifyItemChanged(adapter.selectedPosition)
        }

        if (position < 0 || position >= data.traceList.size) {
            return
        }

        adapter.selectedPosition = position
        // new
        adapter.notifyItemChanged(adapter.selectedPosition)

        // 更新地图选中
        binding.mapView.updateSelect(data.traceList[position])
        LogUtils.i(data.traceList[position])
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

}