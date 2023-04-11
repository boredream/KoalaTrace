package com.boredream.koalatrace.ui.trace.recorddetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseActivity
import com.boredream.koalatrace.common.SimpleUiStateObserver
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.constant.BundleKey
import com.boredream.koalatrace.databinding.ActivityTraceRecordDetailBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TraceRecordDetailActivity :
    BaseActivity<TraceRecordDetailViewModel, ActivityTraceRecordDetailBinding>() {

    private lateinit var data: TraceRecord

    override fun getLayoutId() = R.layout.activity_trace_record_detail

    override fun getViewModelClass() = TraceRecordDetailViewModel::class.java

    companion object {

        fun start(context: Context, data: TraceRecord) {
            val intent = Intent(context, TraceRecordDetailActivity::class.java)
            intent.putExtra(BundleKey.DATA, data)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.mapView.onCreate(savedInstanceState)

        data = intent.extras?.getSerializable(BundleKey.DATA) as TraceRecord
        initObserver()
        viewModel.start(data)
    }

    private fun initObserver() {
        SimpleUiStateObserver.setRequestObserver(
            this,
            this,
            viewModel.requestVMCompose,
            successObserver = {})

        SimpleUiStateObserver.setRequestObserver(
            this,
            this,
            viewModel.commitVMCompose
        )
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