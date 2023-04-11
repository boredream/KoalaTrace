package com.boredream.koalatrace.utils

import android.content.Context
import java.io.IOException
import java.io.InputStream

object FileUtils {

    fun readBytesFromAssets(context: Context, fileName: String): ByteArray? {
        var inputStream: InputStream? = null
        var bytes: ByteArray? = null
        try {
            inputStream = context.assets.open(fileName)
            bytes = inputStream.readBytes()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return bytes
    }
}