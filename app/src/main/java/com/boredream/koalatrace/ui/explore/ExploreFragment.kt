package com.boredream.koalatrace.ui.explore

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.PolylineOptions
import com.amap.api.maps.model.TextOptions
import com.blankj.utilcode.util.CollectionUtils
import com.blankj.utilcode.util.LogUtils
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseFragment
import com.boredream.koalatrace.common.SimpleUiStateObserver
import com.boredream.koalatrace.data.ExploreAreaInfo
import com.boredream.koalatrace.data.TraceRecordArea
import com.boredream.koalatrace.data.constant.MapConstant
import com.boredream.koalatrace.databinding.FragmentExploreBinding
import com.boredream.koalatrace.utils.TraceUtils
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat


@AndroidEntryPoint
class ExploreFragment : BaseFragment<ExploreViewModel, FragmentExploreBinding>() {

    override fun getLayoutId() = R.layout.fragment_explore

    override fun getViewModelClass() = ExploreViewModel::class.java

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        LogUtils.v("map view onCreate")
        binding.mapView.onCreate(savedInstanceState)
        initObserver()
        initData()
        return view
    }

    private fun initData() {
        viewModel.init()
    }

    private fun initObserver() {
        viewModel.loadAreaVMCompose.successUiState.observe(viewLifecycleOwner) {areaList ->
            val menuList = arrayListOf("请选择区域")
            if (areaList.isSuccess()) {
                menuList.addAll(areaList.getSuccessData()
                    .map { "${it.subAdminArea}-${it.locality}" })
            }
            binding.spinnerArea.setDropMenuDataList(menuList)
            binding.spinnerArea.setOnDropMenuItemClickListener { position, data ->
                if (position > 0) {
                    val recordArea = areaList.getSuccessData()[position - 1]
                    viewModel.drawExplore(recordArea)
                }
            }
        }

        viewModel.loadVMCompose.successUiState.observe(viewLifecycleOwner) {
            testDraw(it.getSuccessData())
        }
    }

    private fun testDraw(data: ExploreAreaInfo) {
        if(CollectionUtils.isEmpty(data.boundaryLatLngList)) {
            return
        }

        binding.mapView.drawExploreArea(data)
    }

    override fun onDestroyView() {
        LogUtils.v("map view onDestroy")
        binding.mapView.onDestroy()
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
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (isAdded && !isHidden) {
            onFragmentPause()
        }
    }

    private fun onFragmentPause() {
        LogUtils.v("map view onPause")
        binding.mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        LogUtils.v("map view onSaveInstanceState")
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

}