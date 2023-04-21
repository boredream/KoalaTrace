package com.boredream.koalatrace

import android.app.Activity
import android.app.Application
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.Utils
import com.boredream.koalatrace.data.constant.GlobalConstant
import com.boredream.koalatrace.utils.DataStoreUtils
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import dagger.hilt.android.HiltAndroidApp
import java.io.File


@HiltAndroidApp
class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Utils.init(this)

        // https://github.com/Blankj/AndroidUtilCode/blob/master/lib/utilcode/README-CN.md#%E6%97%A5%E5%BF%97%E7%9B%B8%E5%85%B3---logutilsjava---demo
        LogUtils.getConfig()
            .setStackOffset(1)
            .setConsoleSwitch(BuildConfig.DEBUG)
//            .setLog2FileSwitch(BuildConfig.DEBUG)
//            .setDir(File(PathUtils.getInternalAppCachePath(), "log"))
//            .setSaveDays(7)
            .setBorderSwitch(false)

        DataStoreUtils.init(this)
        initRefresh()

        AppUtils.registerAppStatusChangedListener(object : Utils.OnAppStatusChangedListener {
            override fun onForeground(activity: Activity?) {
                GlobalConstant.isForeground = true
            }

            override fun onBackground(activity: Activity?) {
                GlobalConstant.isForeground = false
            }
        })
    }

    private fun initRefresh() {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
            layout.setPrimaryColorsId(R.color.white)//全局设置主题颜色  CustomRefreshHeader   ClassicsHeader
            ClassicsHeader(context)//.setTimeFormat(new DynamicTimeFormat("更新于 %s"));//指定为经典Header，默认是 贝塞尔雷达Header
        }
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
            //指定为经典Footer，默认是 BallPulseFooter
            ClassicsFooter(context).setDrawableSize(20f)
        }
    }

}