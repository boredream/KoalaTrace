package com.boredream.koalatrace.utils

import com.blankj.utilcode.util.LogUtils
import javax.inject.Inject

open class Logger @Inject constructor() {

    open fun i(log: String) {
        LogUtils.i(log)
    }

}