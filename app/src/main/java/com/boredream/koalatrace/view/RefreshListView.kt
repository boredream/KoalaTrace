package com.boredream.koalatrace.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.boredream.koalatrace.R
import com.boredream.koalatrace.base.BaseListAdapter
import com.boredream.koalatrace.common.vmcompose.RefreshUiState
import com.boredream.koalatrace.databinding.ViewRefreshListBinding
import com.scwang.smart.refresh.layout.SmartRefreshLayout

class RefreshListView : FrameLayout {

    var dataBinding: ViewRefreshListBinding
    val refresh: SmartRefreshLayout
    val rv: RecyclerView
    private var adapter: BaseListAdapter<Any, ViewDataBinding>? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        dataBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.view_refresh_list,
            this,
            true
        )

        refresh = dataBinding.refreshListRefresh
        rv = dataBinding.refreshListRv
    }

    @Suppress("UNCHECKED_CAST")
    fun <T, BD : ViewDataBinding> setup(
        adapter: BaseListAdapter<T, BD>,
        enableLoadMore: Boolean = false,
        onLoadMore: () -> Unit = {},
        enableRefresh: Boolean = true,
        onRefresh: () -> Unit = {},
        layoutManager: LinearLayoutManager = LinearLayoutManager(context),
        itemDecoration: ItemDecoration? = DividerItemDecoration(context, VERTICAL),
    ) {
        refresh.setEnableLoadMore(enableLoadMore)
        refresh.setOnLoadMoreListener { onLoadMore.invoke() }
        refresh.setEnableRefresh(enableRefresh)
        refresh.setOnRefreshListener { onRefresh.invoke() }

        rv.layoutManager = layoutManager
        itemDecoration?.let { rv.addItemDecoration(it) }
        rv.adapter = adapter

        this.adapter = adapter as BaseListAdapter<Any, ViewDataBinding>
    }

    fun updateRefreshState(refreshState: RefreshUiState) {
        // TODO: 非手动下拉的刷新请求，应该换个样式，不用autoRefresh
        refresh.setEnableLoadMore(refreshState.enableLoadMore)
        if (refreshState.showRefresh) refresh.autoRefresh() else refresh.finishRefresh()
        if (refreshState.showLoadMore) refresh.autoLoadMore() else refresh.finishLoadMore()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateDataList(dataList: ArrayList<*>) {
        adapter?.let { adapter ->
            adapter.dataList.clear()
            adapter.dataList.addAll(dataList)
            adapter.notifyDataSetChanged()
        }
    }

}
