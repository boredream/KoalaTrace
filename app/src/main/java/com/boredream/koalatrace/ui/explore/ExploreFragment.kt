package com.boredream.koalatrace.ui.explore

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.PolygonOptions
import com.blankj.utilcode.util.LogUtils
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseFragment
import com.boredream.koalatrace.data.ExploreAreaInfo
import com.boredream.koalatrace.databinding.FragmentExploreBinding
import com.boredream.koalatrace.utils.TraceUtils
import dagger.hilt.android.AndroidEntryPoint


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
            .target(LatLng(
                data.boundaryLatLngList[0].latitude,
                data.boundaryLatLngList[0].longitude))
            .zoom(12f)
            .build()
        binding.mapView.map.moveCamera(CameraUpdateFactory.newCameraPosition(position))

        binding.mapView.map.addPolygon(
            PolygonOptions()
                .addAll(data.boundaryLatLngList)
                .fillColor(Color.argb(30, 0, 0, 255))
                .strokeWidth(0f)
        )

        data.blockList.forEach { blockInfo ->
            val actualBoundaryStrList = blockInfo.actualBoundary.split("==")
            actualBoundaryStrList.forEach {
                binding.mapView.map.addPolygon(PolygonOptions()
                    .addAll(TraceUtils.str2LatLngList(it))
                    .fillColor(Color.argb(30, 0, 255, 0))
                    .strokeWidth(0f))
            }
        }
    }

    override fun onDestroyView() {
        LogUtils.v("map view onDestroy")
        binding.mapView.onDestroy()
        super.onDestroyView()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if(isAdded) {
            if(hidden) onFragmentPause()
            else onFragmentResume()
        }
    }

    override fun onResume() {
        super.onResume()
        if(isAdded && !isHidden) {
            onFragmentResume()
        }
    }

    private fun onFragmentResume() {
        LogUtils.v("map view onResume")
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        if(isAdded && !isHidden) {
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