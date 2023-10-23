package com.boredream.koalatrace.ui.explore

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.PolylineOptions
import com.amap.api.maps.model.TextOptions
import com.blankj.utilcode.util.LogUtils
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseFragment
import com.boredream.koalatrace.data.ExploreAreaInfo
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
        return view
    }

    private fun initObserver() {
        // FIXME: remove me
        binding.titleBar.dataBinding.ivLeft.visibility = View.VISIBLE
        binding.titleBar.dataBinding.ivLeft.setOnClickListener {
            viewModel.drawExplore()
        }

        viewModel.loadVMCompose.successUiState.observe(viewLifecycleOwner) {
            testDraw(it.data!!)
        }
    }

    private fun testDraw(data: ExploreAreaInfo) {
        val position = CameraPosition.Builder()
            .target(
                LatLng(
                    data.boundaryLatLngList[0].latitude,
                    data.boundaryLatLngList[0].longitude
                )
            )
            .zoom(12f)
            .build()
        binding.mapView.map.moveCamera(CameraUpdateFactory.newCameraPosition(position))

        // 整个区域绘制迷雾，已探索区域挖孔
        TraceUtils.drawJstPolygonMask(
            binding.mapView.map,
            data.boundaryLatLngList,
            data.explorePolygon,
            MapConstant.FROG_COLOR
        )

        // 区域内每个区块
        val format = DecimalFormat("0.#")
        data.blockList.forEach { blockInfo ->
            // 绘制边界
            blockInfo.actualBoundary.split("==").forEach {
                binding.mapView.map.addPolyline(
                    PolylineOptions()
                        .addAll(TraceUtils.str2LatLngList(it))
                        .color(Color.argb(255, 255, 255, 0))
                        .width(2f)
                        .zIndex(MapConstant.FROG_MAP_Z_INDEX + 1f)
                )
            }

            // 绘制探索信息
            val rectLatLngList = TraceUtils.str2LatLngList(blockInfo.rectBoundary)
            val center = LatLng(
                (rectLatLngList[0].latitude + rectLatLngList[2].latitude) / 2,
                (rectLatLngList[0].longitude + rectLatLngList[2].longitude) / 2
            )
            binding.mapView.map.addText(
                TextOptions()
                    .position(center)
                    .text(format.format(blockInfo.explorePercent * 100) + "%")
                    .zIndex(MapConstant.FROG_MAP_Z_INDEX + 1f)
            )
        }
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