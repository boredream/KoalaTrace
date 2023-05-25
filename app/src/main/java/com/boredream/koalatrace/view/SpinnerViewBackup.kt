package com.boredream.koalatrace.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import com.boredream.koalatrace.R
import com.boredream.koalatrace.databinding.ViewSpinnerBinding


class SpinnerViewBackup : LinearLayout, View.OnClickListener {

    private val dataBinding: ViewSpinnerBinding
    private lateinit var popupWindow: PopupWindow
    private lateinit var dropMenuView: SpinnerDropMenuView
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
            R.layout.view_spinner,
            this,
            true
        )

        gravity = Gravity.CENTER_VERTICAL
        orientation = HORIZONTAL

        initPopupWindow()
        setOnClickListener(this)
    }

    private fun initPopupWindow() {
        // 创建PopupWindow
        dropMenuView = SpinnerDropMenuView(context)
        dropMenuView.onItemClickListener = { position, data ->
            // 选中下拉项后，回调之，以及隐藏pop
            dataBinding.tvTitle.text = data
            onItemClickListener.invoke(position, data)
            popupWindow.dismiss()
        }
        popupWindow = PopupWindow(
            dropMenuView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        popupWindow.isFocusable = true
    }

    fun setOnDropMenuItemClickListener(onItemClickListener: (position: Int, t: String) -> Unit) {
        this.onItemClickListener = onItemClickListener
    }

    fun setDropMenuDataList(dataList: List<String>, defSelect: Int = 0) {
        dropMenuView.setDataList(dataList, defSelect)
    }

    fun setSelectItem(selected: Int) {
        dropMenuView.setSelectItem(selected)
    }

    override fun onClick(v: View?) {
        showDropDownMenu()
    }

    private fun showDropDownMenu() {
        popupWindow.showAsDropDown(this)
    }

}