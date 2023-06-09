package com.boredream.koalatrace.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.blankj.utilcode.util.LogUtils
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseActivity
import com.boredream.koalatrace.base.BaseFragment
import com.boredream.koalatrace.data.constant.BundleKey
import com.boredream.koalatrace.databinding.ActivityMainTabBinding
import com.boredream.koalatrace.service.TraceLocationService
import com.boredream.koalatrace.ui.FragmentController
import com.boredream.koalatrace.ui.home.HomeFragment
import com.boredream.koalatrace.ui.mine.MineFragment
import com.boredream.koalatrace.ui.recordlist.TraceRecordListFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainTabActivity : BaseActivity<MainTabViewModel, ActivityMainTabBinding>() {

    private lateinit var serviceIntent: Intent

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, MainTabActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getLayoutId() = R.layout.activity_main_tab
    override fun getViewModelClass() = MainTabViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navView: BottomNavigationView = binding.navView
        val fragmentList = ArrayList<BaseFragment<*, *>>()
        fragmentList.add(HomeFragment())
        fragmentList.add(TraceRecordListFragment())
        fragmentList.add(MineFragment())

        val controller = FragmentController(navView, supportFragmentManager, R.id.fl_fragment, fragmentList)
        controller.initFragment(savedInstanceState)

        LogUtils.i("----------------------- start ------------------------")
    }

    override fun onResume() {
        super.onResume()
        toggleLocation(true)
    }

    override fun onPause() {
        super.onPause()
        toggleLocation(false)
    }

    private fun toggleLocation(start: Boolean) {
        serviceIntent = Intent(this, TraceLocationService::class.java)
        serviceIntent.putExtra(BundleKey.TOGGLE_LOCATION, start)
        startService(serviceIntent)
    }

}