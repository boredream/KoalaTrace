package com.boredream.koalatrace.base

import com.boredream.koalatrace.data.ResponseEntity
import com.boredream.koalatrace.net.ApiService
import com.boredream.koalatrace.net.LocalImageFilter

/**
 * 请求类 Repo
 */
abstract class BaseRequestFileRepository<T : BaseEntity>(private val service: ApiService) :
    BaseRequestRepository<T>() {

    // TODO: 请求应该放在DataSource里更合适？

    /**
     * 提交请求
     * @param request SuspendFunction0<ResponseEntity<Boolean>> 请求
     * @return ResponseEntity<Boolean> 相应
     */
    protected suspend fun commitWithFile(
        originData: T,
        request: suspend (data: T) -> ResponseEntity<Boolean>
    ): ResponseEntity<Boolean> {
        var data = originData
        var response: ResponseEntity<Boolean>
        try {
            data = checkUploadLocalFile(data)
            response = request.invoke(data)
        } catch (e: Exception) {
            response = ResponseEntity.httpError(e)
        }
        if (response.isSuccess()) {
            // 修改成功后，缓存需要重新刷新
            cacheIsDirty = true
        }
        return response
    }

    // 校验参数里是否有本地文件路径，有的话自动上传后路径替换成url
    private suspend fun checkUploadLocalFile(originData: T): T {
        return LocalImageFilter.checkImage4update(originData, service)
    }

}