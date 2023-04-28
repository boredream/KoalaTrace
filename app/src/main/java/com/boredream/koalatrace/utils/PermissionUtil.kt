package com.boredream.koalatrace.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.LogUtils
import com.permissionx.guolindev.PermissionX

object PermissionUtil {

    fun hasAll(context: Context, permissions: List<String>): Boolean {
        return permissions.all { has(context, it) }
    }

    fun has(context: Context, permission: String): Boolean {
        val granted = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
        // LogUtils.i("$permission is granted: $granted")
        return granted
    }

    fun request(
        activity: AppCompatActivity,
        permissions: List<String>,
        requestSuccess: (apply: Boolean) -> Unit
    ) {
        PermissionX.init(activity)
            .permissions(permissions)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(deniedList, "为了保证应用正常使用，请开启以下权限", "开启", "拒绝")
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(deniedList, "你需要手动前往设置中心开启权限", "前往", "取消")
            }
            .request { allGranted, grantedList, deniedList -> requestSuccess.invoke(allGranted) }
    }

}

