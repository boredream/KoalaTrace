package com.boredream.koalatrace.ui.log

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.FileUtils
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.autonavi.base.amap.mapcore.FileUtil
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.ToastUtils
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseActivity
import com.boredream.koalatrace.base.RepoCacheHelper
import com.boredream.koalatrace.common.SimpleListAdapter
import com.boredream.koalatrace.common.SimpleUiStateObserver
import com.boredream.koalatrace.data.SettingItem
import com.boredream.koalatrace.databinding.ActivityLogBinding
import com.boredream.koalatrace.databinding.ItemLogFileBinding
import com.boredream.koalatrace.databinding.ItemSettingBinding
import com.boredream.koalatrace.ui.log.LogActivity
import com.boredream.koalatrace.ui.log.LogViewModel
import com.boredream.koalatrace.ui.main.MainTabActivity
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@AndroidEntryPoint
class LogActivity : BaseActivity<LogViewModel, ActivityLogBinding>() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, LogActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var dataList = ArrayList<File>()
    private lateinit var adapter: SimpleListAdapter<File, ItemLogFileBinding>

    override fun getLayoutId() = R.layout.activity_log

    override fun getViewModelClass() = LogViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initObserver()
        viewModel.start()
    }

    private fun initView() {
        binding.rv.layoutManager = LinearLayoutManager(this)
        adapter = SimpleListAdapter(dataList, R.layout.item_log_file)
        adapter.onItemClickListener = {
            val log = FileIOUtils.readFile2String(it)
            binding.tvLog.text = log
            binding.svLog.visibility = View.VISIBLE
        }
        binding.rv.adapter = adapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserver() {
        viewModel.uiState.observe(this) {
            dataList.clear()
            dataList.addAll(it)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onBackPressed() {
        if(binding.svLog.visibility == View.VISIBLE) {
            binding.svLog.visibility = View.GONE
        } else {
            super.onBackPressed()
        }
    }
}