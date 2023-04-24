package com.boredream.koalatrace.base

open class BaseLiveEvent

data class ToastLiveEvent(
    val toast: String
) : BaseLiveEvent()

data class ShowConfirmDialogLiveEvent(
    val title: String = "提醒",
    val content: String = "确定操作吗？",
    var confirmText: String = "确定",
    var cancelText: String = "取消",
    var confirmClickListener: () -> Unit = {}
) : BaseLiveEvent()
