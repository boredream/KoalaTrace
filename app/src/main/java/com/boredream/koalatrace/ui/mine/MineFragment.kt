package com.boredream.koalatrace.ui.mine

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.AppUtils
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseFragment
import com.boredream.koalatrace.common.SimpleListAdapter
import com.boredream.koalatrace.data.SettingItem
import com.boredream.koalatrace.databinding.FragmentMineBinding
import com.boredream.koalatrace.databinding.ItemSettingBinding
import com.boredream.koalatrace.ui.log.LogActivity
import com.boredream.koalatrace.ui.login.LoginActivity
import com.boredream.koalatrace.utils.PermissionUtil
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MineFragment : BaseFragment<MineViewModel, FragmentMineBinding>() {

    override fun getLayoutId() = R.layout.fragment_mine
    override fun getViewModelClass() = MineViewModel::class.java

    private var dataList = ArrayList<SettingItem>()
    private lateinit var adapter: SimpleListAdapter<SettingItem, ItemSettingBinding>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        initList()
        initObserver()
        viewModel.loadUserInfo()
        return view
    }

    private fun initList() {
        binding.rvSetting.layoutManager = LinearLayoutManager(activity)
        dataList.add(SettingItem("关于我们", ""))
        dataList.add(SettingItem("推荐给好友", ""))
        dataList.add(SettingItem("意见反馈", ""))
        dataList.add(SettingItem("日志", ""))
        dataList.add(SettingItem("备份数据库", ""))
        dataList.add(SettingItem("恢复数据库", ""))
        adapter = SimpleListAdapter(dataList, R.layout.item_setting)
        adapter.onItemClickListener = {
            when (it.name) {
                "关于我们" -> { }
                "日志" -> startActivity(Intent(activity, LogActivity::class.java))
                "备份数据库" -> checkPermission(viewModel::backupDB)
                "恢复数据库" -> checkPermission(viewModel::showRestoreDbConfirmDialog)
            }
        }
        binding.rvSetting.adapter = adapter
    }

    private fun checkPermission(callback: () -> Unit) {
        val permissions = mutableListOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissions.add(MANAGE_EXTERNAL_STORAGE)
        }
        PermissionUtil.request(baseActivity, permissions) {
            if (it) callback.invoke()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserver() {
        viewModel.eventUiState.observe(viewLifecycleOwner) {
            when (it) {
                is LogoutEvent -> {
                    LoginActivity.start(requireContext())
                    baseActivity.finish()
                }
                is RestoreSuccessEvent -> AppUtils.relaunchApp(true)
            }
        }
    }

}