package com.boredream.koalatrace.ui.mine

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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
import com.boredream.koalatrace.utils.PermissionSettingUtil
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.runtime.Permission
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
                "日志" -> startActivity(Intent(activity, LogActivity::class.java))
                "备份数据库" -> backupDB()
                "恢复数据库" -> restoreDB()
            }
        }
        binding.rvSetting.adapter = adapter
    }

    private fun backupDB() {
        // FIXME: 当targetSDK从29升级到33后，前台服务端的通知不显示了，这里的写入文件也会失效？
        AndPermission.with(this)
            .runtime()
            .permission(
                Permission.WRITE_EXTERNAL_STORAGE,
            )
            .onGranted {
               viewModel.backupDB()
            }
            .onDenied { permissions ->
                if (AndPermission.hasAlwaysDeniedPermission(this, permissions)) {
                    PermissionSettingUtil.showSetting(requireContext(), permissions)
                }
            }
            .start()
    }

    private fun restoreDB() {
        AndPermission.with(this)
            .runtime()
            .permission(
                Permission.WRITE_EXTERNAL_STORAGE,
            )
            .onGranted {
                viewModel.showRestoreDbConfirmDialog()
            }
            .onDenied { permissions ->
                if (AndPermission.hasAlwaysDeniedPermission(this, permissions)) {
                    PermissionSettingUtil.showSetting(requireContext(), permissions)
                }
            }
            .start()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserver() {
        viewModel.uiState.observe(viewLifecycleOwner) {
            // 更新用户绑定另一半信息
            val bindCpItem = dataList[0]
            bindCpItem.content = if (it.cpUser != null) ("昵称：" + it.cpUser?.nickname) else "点击绑定"
            adapter.notifyDataSetChanged()
        }

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