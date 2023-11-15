package com.boredream.koalatrace.service

import android.app.job.JobParameters
import android.app.job.JobService
import com.blankj.utilcode.util.LogUtils

class LogMonitoringJobService : JobService() {

    override fun onStartJob(params: JobParameters): Boolean {
        LogUtils.i("===== I AM ALIVE! =====")

        // 在这里执行你的监控逻辑，比如记录日志

        // 任务完成后调用 jobFinished() 方法
        jobFinished(params, false)

        // 返回 true 表示任务仍在进行，如果任务是异步的，需要手动调用 jobFinished() 方法来通知任务完成
        // 如果任务是同步的，可以直接返回 false
        return false
    }

    override fun onStopJob(params: JobParameters): Boolean {
        // 返回 true 表示需要重新安排任务
        // 返回 false 表示任务已完成或不需要重新安排
        return false
    }
}