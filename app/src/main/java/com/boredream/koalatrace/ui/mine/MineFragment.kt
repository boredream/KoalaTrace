package com.boredream.koalatrace.ui.mine

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.IntentUtils
import com.boredream.koalatrace.BuildConfig
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseFragment
import com.boredream.koalatrace.common.SimpleListAdapter
import com.boredream.koalatrace.data.SettingItem
import com.boredream.koalatrace.databinding.FragmentMineBinding
import com.boredream.koalatrace.databinding.ItemSettingBinding
import com.boredream.koalatrace.ui.log.LogActivity
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
        return view
    }

    private fun initList() {
        binding.rvSetting.layoutManager = LinearLayoutManager(activity)
        dataList.add(SettingItem("权限", "")) // TODO: 待补充
        dataList.add(SettingItem("使用教程", "")) // TODO: 待补充
        dataList.add(SettingItem("推荐给好友", ""))
        dataList.add(SettingItem("意见反馈", "")) // TODO: 待补充
        dataList.add(SettingItem("备份数据库", ""))
        dataList.add(SettingItem("恢复数据库", ""))
        dataList.add(SettingItem("关于", ""))
        if (BuildConfig.DEBUG) {
            dataList.add(SettingItem("日志", ""))
        }
        adapter = SimpleListAdapter(dataList, R.layout.item_setting)
        adapter.onItemClickListener = { _, it ->
            when (it.name) {
                "推荐给好友" -> startActivity(IntentUtils.getShareTextIntent("我在使用一块好用的软件，快来下载吧! \nhttps://www.papikoala.cn")) // TODO: 待补充
                "意见反馈" -> {
                    Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("boredream@qq.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "【${AppUtils.getAppName()}】意见反馈")
                        putExtra(Intent.EXTRA_TEXT, "请在邮箱中输入您的意见：")
                        startActivity(Intent.createChooser(this, "发送邮件"))
                    }
                }
                "备份数据库" -> checkPermission(viewModel::backupDB)
                "恢复数据库" -> checkPermission(viewModel::showRestoreDbConfirmDialog)
                "日志" -> startActivity(Intent(activity, LogActivity::class.java))
            }
        }
        binding.rvSetting.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        updatePermissionInfo()
    }

    private fun updatePermissionInfo() {
        val permissions = mutableListOf(
            ACCESS_FINE_LOCATION,
            WRITE_EXTERNAL_STORAGE,
            READ_EXTERNAL_STORAGE
        )
        dataList[0].content = if (PermissionUtil.hasAll(baseActivity, permissions))
            "已开启全部权限" else "未开启全部权限"
        adapter.notifyItemChanged(0)
    }

    // TODO: target api > 29 之后需要处理
//    private val launcher =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//            if (it.resultCode == Activity.RESULT_OK) {
//                val data = it.data
//                // 处理返回结果
//                LogUtils.i(data)
//            }
//        }
//
//    private fun checkPermission2() {
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
//        intent.putExtra(Intent.EXTRA_MIME_TYPES, "application/octet-stream");
//        launcher.launch(intent)
//    }

    private fun checkPermission(callback: () -> Unit) {
        val permissions = mutableListOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE)
        PermissionUtil.request(baseActivity, permissions) {
            if (it) callback.invoke()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserver() {
        viewModel.eventUiState.observe(viewLifecycleOwner) {
            when (it) {
                is RestoreSuccessEvent -> AppUtils.relaunchApp(true)
            }
        }
    }

}