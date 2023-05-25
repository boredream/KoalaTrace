package com.boredream.koalatrace.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseListAdapter
import com.boredream.koalatrace.databinding.ItemSelectedTextBinding
import com.boredream.koalatrace.databinding.ViewSpinnerDropMenuBinding


class SpinnerDropMenuView : FrameLayout {

    private val dataBinding: ViewSpinnerDropMenuBinding
    private val adapter: Adapter
    var onItemClickListener: (position: Int, t: String) -> Unit = { _, _ -> }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        dataBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.view_spinner_drop_menu,
            this,
            true
        )

        setBackgroundColor(Color.WHITE)

        dataBinding.rvDropMenu.layoutManager = LinearLayoutManager(context)
        adapter = Adapter(arrayListOf())
        adapter.onItemClickListener = {position, data ->
            onItemClickListener.invoke(position, data)
        }
        dataBinding.rvDropMenu.adapter = adapter
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setDataList(dataList: List<String>, defSelect: Int = 0) {
        adapter.selectedPosition = defSelect

        adapter.dataList.clear()
        adapter.dataList.addAll(dataList)
        adapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectItem(selected: Int) {
        adapter.selectedPosition = selected
        adapter.notifyDataSetChanged()
    }

    class Adapter(dataList: ArrayList<String>) : BaseListAdapter<String, ItemSelectedTextBinding>(dataList) {

        var selectedPosition: Int = -1

        override fun getItemLayoutId() = R.layout.item_selected_text

        override fun setItemData(position: Int, binding: ItemSelectedTextBinding, data: String) {
            binding.tvInfo.text = data
            binding.ivSelected.visibility = if(position == selectedPosition) View.VISIBLE else View.GONE
        }
    }

}