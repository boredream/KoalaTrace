package com.boredream.koalatrace.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.blankj.utilcode.util.LogUtils


/**
 * https://developer.android.google.cn/guide/topics/appwidgets?hl=en#AppWidgetProvider
 * AppWidgetProvider 类扩展了 BroadcastReceiver 作为一个辅助类来处理应用微件广播。AppWidgetProvider 仅接收与应用微件有关的事件广播，例如当更新、删除、启用和停用应用微件时发出的广播。
 */
class AppWidgetProvider : AppWidgetProvider() {

    // 桌面widget创建时、updatePeriodMillis定时触发时 回调本方法
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        AppWidgetUpdater.updateDefault(context)
    }

}