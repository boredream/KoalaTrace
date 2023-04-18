package com.boredream.koalatrace.utils

import android.util.Log
import com.blankj.utilcode.util.LogUtils
import javax.inject.Inject

open class Logger @Inject constructor() {

    open fun v(log: String) {
        Log.v("DDD", log)
    }

    open fun i(log: String) {
        Log.i("DDD", log)
    }

}