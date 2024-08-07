package com.boredream.koalatrace.service

import android.app.*
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.blankj.utilcode.util.LogUtils
import com.boredream.koalatrace.R
import com.boredream.koalatrace.data.constant.BundleKey
import com.boredream.koalatrace.data.repo.LocationRepository
import com.boredream.koalatrace.data.usecase.TraceUseCase
import com.boredream.koalatrace.ui.main.MainTabActivity
import com.boredream.koalatrace.utils.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * 追踪前台服务，保证切换到后台时，依然可以继续定位
 * https://developer.android.com/guide/components/foreground-services
 */
@AndroidEntryPoint
class TraceLocationService : Service() {

    companion object {
        const val SERVICE_ID = 19900214
        const val CHANNEL_ID = "com.boredream.koalatrace.service.tracelocation"
        const val LOG_JOB_ID = 1122334
    }

    @Inject
    lateinit var traceUseCase: TraceUseCase

    @Inject
    lateinit var scope: CoroutineScope

    @Inject
    lateinit var logger: Logger

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        val intent = Intent(this, MainTabActivity::class.java)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(application, 0, intent, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(application, 0, intent, FLAG_UPDATE_CURRENT)
        }

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANNEL_ID, getString(R.string.app_name), IMPORTANCE_DEFAULT)
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_route_24) // 设置通知的图标
                .setContentTitle(getString(R.string.app_name)) // 设置标题的标题
                .setContentText("记录轨迹ing...") // 设置的标题内容
                .setContentIntent(pendingIntent)
                .build()
        } else {
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_route_24)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("记录轨迹ing...")
                .setContentIntent(pendingIntent)
                .build()
        }

        // 只做保活用，无需任何处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(SERVICE_ID, notification, FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(SERVICE_ID, notification)
        }

        traceUseCase.addStatusChangeListener {
            val status = when(it) {
                LocationRepository.STATUS_IDLE -> "STATUS_IDLE"
                LocationRepository.STATUS_LOCATION -> "STATUS_LOCATION"
                LocationRepository.STATUS_TRACE -> "STATUS_TRACE"
                else -> "unknown"
            }
            logger.i("location status = $status")
        }
        traceUseCase.startLocation()
        scope.launch {
            traceUseCase.startTrace()
        }

        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val jobInfo = JobInfo.Builder(LOG_JOB_ID, ComponentName(this, LogMonitoringJobService::class.java))
            .setPeriodic(30 * 60 * 1000) // 设置监控任务的执行间隔，这里是半个小时（30分钟 * 60秒 * 1000毫秒）
            .build()
        jobScheduler.schedule(jobInfo)

        logger.i("Trance Location Service onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            if (it.hasExtra(BundleKey.TOGGLE_TRACE)) {
                val start = it.getBooleanExtra(BundleKey.TOGGLE_TRACE, false)
                scope.launch {
                    if (start) {
                        traceUseCase.startTrace()
                    } else {
                        // 打开页面/刷新widget进行询问保存？或者直接进行保存
                        traceUseCase.stopTrace()
                    }
                }
            }

            if (it.hasExtra(BundleKey.TOGGLE_LOCATION)) {
                val start = it.getBooleanExtra(BundleKey.TOGGLE_LOCATION, false)
                if (start) {
                    traceUseCase.startLocation()
                } else {
                    traceUseCase.stopLocation()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

}