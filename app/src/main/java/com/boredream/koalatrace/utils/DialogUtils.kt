package com.boredream.koalatrace.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog

object DialogUtils {

    fun showDeleteConfirmDialog(
        context: Context,
        okListener: () -> Unit,
        cancelListener: () -> Unit = { }
    ) {
        AlertDialog.Builder(context)
            .setTitle("提醒")
            .setMessage("是否确认删除")
            .setPositiveButton("确认") { _, _ -> okListener.invoke() }
            .setNegativeButton("取消") { _, _ -> cancelListener.invoke() }
            .show()
    }

}