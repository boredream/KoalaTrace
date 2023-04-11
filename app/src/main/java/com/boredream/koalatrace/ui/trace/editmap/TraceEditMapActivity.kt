package com.boredream.koalatrace.ui.trace.editmap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseActivity
import com.boredream.koalatrace.data.TraceRecord
import com.boredream.koalatrace.data.constant.BundleKey
import com.boredream.koalatrace.data.repo.TraceRecordRepository
import com.boredream.koalatrace.databinding.ActivityTraceEditMapBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class TraceEditMapActivity : BaseActivity<TraceEditMapViewModel, ActivityTraceEditMapBinding>() {

    private lateinit var data: TraceRecord

    override fun getLayoutId() = R.layout.activity_trace_edit_map

    override fun getViewModelClass() = TraceEditMapViewModel::class.java

    companion object {

        fun start(context: Context, data: TraceRecord) {
            val intent = Intent(context, TraceEditMapActivity::class.java)
            intent.putExtra(BundleKey.DATA, data)
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var repo : TraceRecordRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.mapView.onCreate(savedInstanceState)

        // data = intent.extras?.getSerializable(BundleKey.DATA) as TraceRecord
        viewModel.start(data)
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