package com.boredream.koalatrace.utils


class PrintLogger : Logger() {

    override fun i(log: String) {
        println("PrintLogger: $log")
    }

    override fun v(log: String) {
        println("PrintLogger: $log")
    }

}