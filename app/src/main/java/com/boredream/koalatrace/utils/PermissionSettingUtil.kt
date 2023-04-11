package com.boredream.koalatrace.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.runtime.Permission


object PermissionSettingUtil {

    fun showSetting(context: Context, permissions: List<String>) {
        val permissionNames: List<String> = Permission.transformText(context, permissions)
        val sbMsg = StringBuilder()
        sbMsg.append("需要以下权限，请在设置中开启：")
        for (permissionName in permissionNames) {
            sbMsg.append("\n").append(permissionName)
        }

        val setting = AndPermission.with(context).runtime().setting()
        AlertDialog.Builder(context)
            .setTitle("权限提示")
            .setMessage(sbMsg.toString())
            .setPositiveButton("去设置") { _, _ -> setting.start(0) }
            .setNegativeButton("不同意", null)
            .show()
    }

}