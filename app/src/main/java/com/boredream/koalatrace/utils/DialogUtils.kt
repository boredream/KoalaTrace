package com.boredream.koalatrace.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog

object DialogUtils {

    fun showDeleteConfirmDialog(
        context: Context,
        okListener: () -> Unit,
        cancelListener: () -> Unit = { }
    ) {
        showDialog(
            context,
            message = "是否确认删除",
            okListener = okListener,
            cancelListener = cancelListener
        )
    }

    fun showDialog(
        context: Context,
        title: String = "提醒",
        message: String = "",
        okText: String = "确认",
        okListener: () -> Unit,
        cancelText: String = "取消",
        cancelListener: () -> Unit = { }
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(okText) { _, _ -> okListener.invoke() }
            .setNegativeButton(cancelText) { _, _ -> cancelListener.invoke() }
            .show()
    }

}