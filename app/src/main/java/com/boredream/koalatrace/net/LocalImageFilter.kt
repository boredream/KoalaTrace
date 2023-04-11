package com.boredream.koalatrace.net

import com.blankj.utilcode.util.CollectionUtils
import com.boredream.koalatrace.data.dto.FileUploadPolicyDTO
import com.boredream.koalatrace.exception.CustomerException
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*

// 该类用于把对象里本地图片地址上传后替换成网络图片地址。
// 参考大少逻辑尝试修改。
object LocalImageFilter {

    suspend fun <T : Any> checkImage4update(
        data: T,
        service: ApiService
    ): T {
        val queue: Queue<JeInfo> = LinkedList()
        val element = JsonParser().parse(Gson().toJson(data))
        queue.add(JeInfo(null, null, element))

        // 解析数据，记录本地图片字段
        val localImagePathJeList: MutableList<JeInfo> = ArrayList()
        while (!queue.isEmpty()) {
            val size: Int = queue.size
            for (i in 0 until size) {
                val info: JeInfo = queue.poll() ?: continue
                if (info.je.isJsonArray) {
                    // 数组循环添加，元素可能是对象也可能是字符串
                    val array = info.je.asJsonArray
                    for (index in 0 until array.size()) {
                        queue.add(JeInfo(info.je, index, array[index]))
                    }
                } else if (info.je.isJsonObject) {
                    // 对象继续拆变量添加
                    for ((key, value) in info.je.asJsonObject.entrySet()) {
                        queue.add(JeInfo(info.je, key, value))
                    }
                } else if (info.je.isJsonPrimitive && info.je.asJsonPrimitive.isString) {
                    // 字符串类型
                    val str = info.je.asString
                    // 初步判断，如果是本地图片字段，记录之
                    if (hasLocalFilePath(str)) {
                        localImagePathJeList.add(info)
                    }
                }
            }
        }

        // 上传本地图片后替换成url
        if (!CollectionUtils.isEmpty(localImagePathJeList)) {
            // 先获取上传凭证
            val uploadPolicy = service.getUploadPolicy()
            if (!uploadPolicy.isSuccess()) {
                // 如果凭证获取失败，则直接抛出异常
                throw CustomerException("上传失败 getUploadPolicy error")
            }

            for (info in localImagePathJeList) {
                for (path in info.je.asString.split(",").toTypedArray()) {
                    // 可能字段下多个图片，挨个上传逐个替换
                    uploadImage(info, path, uploadPolicy.data!!, service)
                }
            }
        }

        return Gson().fromJson(element, data::class.java)
    }

    // 判断是否包含本地文件路径
    private fun hasLocalFilePath(path: String): Boolean {
        // TODO: 优化逻辑
        return path.contains("/storage/")
    }

    private suspend fun uploadImage(
        info: JeInfo,
        path: String,
        uploadPolicy: FileUploadPolicyDTO,
        service: ApiService,
    ) {
        val file = File(path)
        val fileName = file.name
        val imageBody = RequestBody.create(MediaType.parse("image/*"), file)
        val filePart = MultipartBody.Part.createFormData("file", fileName, imageBody)
        val tokenPart =
            RequestBody.create(MediaType.parse("multipart/form-data"), uploadPolicy.token)
        val fileNamePart = RequestBody.create(MediaType.parse("multipart/form-data"), fileName)
        val response =
            service.uploadFile7niu(uploadPolicy.uploadHost, filePart, tokenPart, fileNamePart)
        val url = response.data!!

        // 将原path，逐个替换成新url
        info.parent?.let {
            if (it.isJsonObject) {
                // 对象，key是字段名
                val obj = it.asJsonObject
                val name = info.key.toString()
                val oldUrl = obj[name].asJsonPrimitive.asString
                val newUrl = oldUrl.replace(path, url)
                obj.addProperty(name, newUrl)
            } else if (it.isJsonArray) {
                // 数组，key是索引
                val array = it.asJsonArray
                val index = info.key.toString().toInt()
                val oldUrl = array[index].asJsonPrimitive.asString
                val newUrl = oldUrl.replace(path, url)
                array[index] = JsonPrimitive(newUrl)
            }
        }
    }

    class JeInfo(var parent: JsonElement?, var key: Any?, var je: JsonElement)
}