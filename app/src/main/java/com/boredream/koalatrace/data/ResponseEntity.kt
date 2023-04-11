package com.boredream.koalatrace.data

data class ResponseEntity<T>(
    val data: T?,
    val code: Int,
    val msg: String
) {
    fun isSuccess() = code == 0

    // TODO: 有更好的写法吗？
    fun getSuccessData() = data!!

    companion object {
        fun <T> success(data: T) : ResponseEntity<T> {
            return ResponseEntity(data, 0, "success")
        }

        fun <T> unknownError() : ResponseEntity<T> {
            return ResponseEntity(null, 600, "未知错误")
        }

        fun <T> notExistError() : ResponseEntity<T> {
            return ResponseEntity(null, 404, "目标不存在")
        }

        fun <T> httpError(e: Exception): ResponseEntity<T> {
            return ResponseEntity(null, 1000, "HTTP 错误: ${e.message}")
        }
    }
}
