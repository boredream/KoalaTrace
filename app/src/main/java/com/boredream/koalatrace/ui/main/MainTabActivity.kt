package com.boredream.koalatrace.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.blankj.utilcode.util.CollectionUtils
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseActivity
import com.boredream.koalatrace.data.constant.BundleKey
import com.boredream.koalatrace.databinding.ActivityMainTabBinding
import com.boredream.koalatrace.service.TraceLocationService
import com.boredream.koalatrace.ui.explore.ExploreFragment
import com.boredream.koalatrace.ui.home.HomeFragment
import com.boredream.koalatrace.ui.mine.MineFragment
import com.boredream.koalatrace.ui.recordlist.TraceRecordListFragment
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

    private val fragmentList = ArrayList<Fragment>()
    private var showFragmentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null && CollectionUtils.isNotEmpty(supportFragmentManager.fragments)) {
            showFragmentIndex = savedInstanceState.getInt("showFragmentIndex")
            fragmentList.addAll(supportFragmentManager.fragments)
        } else {
            // 首次加载
            fragmentList.add(HomeFragment())
            fragmentList.add(TraceRecordListFragment())
            fragmentList.add(ExploreFragment())
            fragmentList.add(MineFragment())

            val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
            for (i in 0 until fragmentList.size) {
                val tag = i.toString()
                ft.add(R.id.fl_fragment, fragmentList[i], tag)
                ft.hide(fragmentList[i])
            }
            ft.commit()
        }

        binding.navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_trace_map -> {
                    showFragment(0)
                    return@setOnItemSelectedListener true
                }

                R.id.navigation_trace_history -> {
                    showFragment(1)
                    return@setOnItemSelectedListener true
                }

                R.id.navigation_trace_explore -> {
                    showFragment(2)
                    return@setOnItemSelectedListener true
                }

                R.id.navigation_mine -> {
                    showFragment(3)
                    return@setOnItemSelectedListener true
                }
            }
            return@setOnItemSelectedListener false
        }

        showFragment(showFragmentIndex)
    }

    private fun showFragment(position: Int) {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.hide(fragmentList[showFragmentIndex])
            .show(fragmentList[position])
            .commit()
        showFragmentIndex = position
    }

    override fun onResume() {
        super.onResume()
        toggleLocation(true)
    }

    override fun onPause() {
        super.onPause()
        toggleLocation(false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("showFragmentIndex", showFragmentIndex)
    }

    private fun toggleLocation(start: Boolean) {
        serviceIntent = Intent(this, TraceLocationService::class.java)
        serviceIntent.putExtra(BundleKey.TOGGLE_LOCATION, start)
        startService(serviceIntent)
    }

}