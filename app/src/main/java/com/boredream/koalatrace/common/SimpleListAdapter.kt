package com.boredream.koalatrace.common

import androidx.databinding.ViewDataBinding
import com.boredream.koalatrace.base.BaseListAdapter
import com.boredream.koalatrace.base.BaseListData

class SimpleListAdapter<T, BD : ViewDataBinding>(
    dataList: ArrayList<T>,
    private val itemLayoutId: Int
) :
    BaseListAdapter<T, BD>(dataList) {

    override fun getItemLayoutId() = itemLayoutId

    override fun getItemId(position: Int): Long {
        val data = dataList[position]
        if (data is BaseListData) {
            return data.getItemId()
        }
        return super.getItemId(position)
    }

}