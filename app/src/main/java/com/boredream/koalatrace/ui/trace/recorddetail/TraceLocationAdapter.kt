package com.boredream.koalatrace.ui.trace.recorddetail

import android.view.View
import com.blankj.utilcode.util.TimeUtils
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseListAdapter
import com.boredream.koalatrace.data.TraceLocation
import com.boredream.koalatrace.databinding.ItemSelectedTextBinding

class TraceLocationAdapter(dataList: ArrayList<TraceLocation>) :
    BaseListAdapter<TraceLocation, ItemSelectedTextBinding>(dataList) {

    var selectedPosition: Int = -1

    override fun getItemLayoutId() = R.layout.item_selected_text

    override fun setItemData(position: Int, binding: ItemSelectedTextBinding, data: TraceLocation) {
        binding.tvInfo.text = TimeUtils.millis2String(data.time, "HH:mm:ss")
        binding.ivSelected.visibility = if(position == selectedPosition) View.VISIBLE else View.GONE
    }

}