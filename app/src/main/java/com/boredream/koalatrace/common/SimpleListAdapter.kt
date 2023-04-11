package com.boredream.koalatrace.common

import androidx.databinding.ViewDataBinding
import com.boredream.koalatrace.base.BaseEntity
import com.boredream.koalatrace.base.BaseListAdapter

class SimpleListAdapter<T, BD : ViewDataBinding>(
    dataList: ArrayList<T>,
    private val itemLayoutId: Int
) :
    BaseListAdapter<T, BD>(dataList) {

    override fun getItemLayoutId() = itemLayoutId

    override fun getItemId(position: Int): Long {
        val data = dataList[position]
        if(data is BaseEntity && data.id != null) {
            return data.id!!.toLong()
        }
        return super.getItemId(position)
    }

}