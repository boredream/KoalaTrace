package com.boredream.koalatrace.view

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.boredream.koalatrace.R

class SyncStatusView : AppCompatImageView {

    private val STATUS_IDLE = 0
    private val STATUS_REFRESHING = 1

    private val refreshAnim: ValueAnimator
    private var curRotate = 0f
    private var status = 0

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        isClickable = true
        setImageResource(R.drawable.ic_baseline_sync_24)
        refreshAnim = ValueAnimator.ofFloat(0f, 1f)
        refreshAnim.repeatCount = ValueAnimator.INFINITE
        refreshAnim.duration = 1000
        refreshAnim.addUpdateListener { animation: ValueAnimator ->
            curRotate = animation.animatedValue as Float
            rotation = -curRotate * 360
        }
    }

    fun setRefresh(isRefreshing: Boolean) {
        if (isRefreshing) startRefresh()
        else finishRefresh()
    }

    fun startRefresh() {
        status = STATUS_REFRESHING
        refreshAnim.start()
    }

    fun finishRefresh() {
        if(status == STATUS_IDLE) return
        refreshAnim.end()
        status = STATUS_IDLE
        rotation = 0f
    }
}