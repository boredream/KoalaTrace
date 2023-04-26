package com.boredream.koalatrace.utils

import androidx.appcompat.app.AppCompatActivity
import com.permissionx.guolindev.PermissionX

object PermissionUtil {

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