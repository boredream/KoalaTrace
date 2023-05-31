package com.boredream.koalatrace.utils

import com.blankj.utilcode.util.LogUtils
import javax.inject.Inject

open class Logger @Inject constructor() {

    open fun v(log: String) {
        LogUtils.v(log)
    }

    open fun i(log: String) {
        LogUtils.i(log)
    }

    open fun e(log: String) {
        LogUtils.e(log)
    }

}