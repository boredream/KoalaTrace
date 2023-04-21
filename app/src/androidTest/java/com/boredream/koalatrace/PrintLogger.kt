package com.boredream.koalatrace

import com.boredream.koalatrace.utils.Logger


class PrintLogger : Logger() {

    override fun i(log: String) {
        print("PrintLogger: $log")
    }

    override fun v(log: String) {
        print("PrintLogger: $log")
    }

}