package com.boredream.koalatrace.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.boredream.koalatrace.BR
import com.boredream.koalatrace.common.BindingViewHolder

abstract class BaseListAdapter<T, BD : ViewDataBinding>(val dataList: ArrayList<T>) :
    RecyclerView.Adapter<BindingViewHolder<BD>>() {

    var onItemClickListener: (position: Int, t: T) -> Unit = { _, _ -> }
    var onItemLongClickListener: (position: Int, t: T) -> Unit ={ _, _ -> }

    protected abstract fun getItemLayoutId(): Int
    protected open fun setItemData(position: Int, binding: BD, data: T) {
        // 大部分数据都MVVM了，这里负责额外处理
        // 如字段二次加工，然后用于 binding.setVariable(BR.bean, data)
        // 或者直接 view.setData
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<BD> {
        val inflater = LayoutInflater.from(parent.context)
        val binding: BD = DataBindingUtil.inflate(inflater, getItemLayoutId(), parent, false)
        return BindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingViewHolder<BD>, position: Int) {
        val data = dataList[position]
        setItemData(position, holder.binding, data)
        holder.binding.setVariable(BR.bean, data)

        holder.itemView.setOnClickListener {
            onItemClickListener.invoke(position, data)
        }
        holder.itemView.setOnLongClickListener {
            onItemLongClickListener.invoke(position, data)
            true
        }
    }

    override fun getItemCount() = dataList.size

}