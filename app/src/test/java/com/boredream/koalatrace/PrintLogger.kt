package com.boredream.koalatrace

import com.boredream.koalatrace.utils.Logger

class PrintLogger : Logger() {

    override fun v(log: String) {
        println(log)
    }

    override fun i(log: String) {
        println(log)
    }

}