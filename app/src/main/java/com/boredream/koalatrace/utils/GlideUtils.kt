package com.boredream.koalatrace.utils

import android.graphics.drawable.ColorDrawable
import android.widget.ImageView
import com.blankj.utilcode.util.ColorUtils
import com.boredream.koalatrace.R
import com.bumptech.glide.RequestManager

// https://muyangmin.github.io/glide-docs-cn/
object GlideUtils {

    // TODO: glide 升级

    /**
     * 加载图片
     *
     * @param glide Glide.with()
     * @param source A file path, or a uri or url handled by com.bumptech.glide.load.model.UriLoader.
     */
    fun load(glide: RequestManager, source: String?, view: ImageView) {
        val defColor = ColorUtils.getColor(R.color.bg_gray)
        glide.load(source)
            .placeholder(ColorDrawable(defColor))
            .error(ColorDrawable(defColor))
            .into(view)
    }

}