package com.boredream.koalatrace.utils

import com.blankj.utilcode.util.LogUtils
import javax.inject.Inject

open class Logger @Inject constructor() {

    private fun decorate(log: String): String {
        if(!log.endsWith("\n")) {
            return log + "\n"
        }
        return log
    }

    open fun v(log: String) {
        LogUtils.v(decorate(log))
    }

    open fun i(log: String) {
        LogUtils.i(decorate(log))
    }

    open fun e(log: String) {
        LogUtils.e(decorate(log))
    }

}