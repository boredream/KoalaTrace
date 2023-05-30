package com.boredream.koalatrace.view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.SizeUtils
import com.boredream.koalatrace.R
import com.boredream.koalatrace.databinding.ViewTitleBarBinding

class TitleBarView : ConstraintLayout {

    companion object {
        @BindingAdapter("rightClick")
        @JvmStatic
        fun setRightClick(view: TitleBarView, callback: () -> Unit) {
            view.dataBinding.tvRight.setOnClickListener { callback.invoke() }
        }
    }

    var dataBinding: ViewTitleBarBinding

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        dataBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.view_title_bar,
            this,
            true
        )

        val a = context.obtainStyledAttributes(attrs, R.styleable.TitleBarView, defStyleAttr, 0)
        val hasBack = a.getBoolean(R.styleable.TitleBarView_hasBack, false)
        dataBinding.tvTitle.text = a.getString(R.styleable.TitleBarView_title)
            ?: resources.getString(R.string.app_name)
        val rightText = a.getString(R.styleable.TitleBarView_rightText)
        rightText?.let { setRightText(it) }
        if (hasBack) {
            dataBinding.ivLeft.visibility = View.VISIBLE
            dataBinding.ivLeft.setOnClickListener {
                if (context is Activity) {
                    context.finish()
                }
            }
        }
        a.recycle()

        minHeight = SizeUtils.dp2px(56f)
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
    }

    fun setRightText(text: CharSequence) {
        dataBinding.tvRight.visibility = VISIBLE
        dataBinding.tvRight.text = text
    }

}