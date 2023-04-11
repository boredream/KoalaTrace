package com.boredream.koalatrace.data

import com.blankj.utilcode.util.StringUtils

data class ImageInfo(
    var url: String? = null,
    var path: String? = null,
) : java.io.Serializable {
    // 优先显示本地图片
    fun getImageSource() = if (!StringUtils.isEmpty(path)) path else url
}