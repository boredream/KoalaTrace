package com.boredream.koalatrace.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.boredream.koalatrace.R
import com.boredream.koalatrace.databinding.ViewSpinnerBinding


class SpinnerView : FrameLayout {

    private val dataBinding: ViewSpinnerBinding

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
    }

    fun setText(text: String) {
        dataBinding.tvTitle.text = text
    }

    fun setSelectStatus(selected: Boolean) {
        if(selected) {
            dataBinding.tvTitle.setTextColor(resources.getColor(R.color.colorPrimary))
        } else {
            dataBinding.tvTitle.setTextColor(resources.getColor(R.color.txt_black))
        }
    }

}