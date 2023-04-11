package com.boredream.koalatrace.net

import android.annotation.SuppressLint
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

object ServiceCreator {

    private const val HOST = "https://www.papikoala.cn/api/"
//    private const val HOST = "http://localhost:8080/api/"
    private const val TOKEN_KEY = "token"
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 10L

    lateinit var tokenFactory: () -> String

    private fun create(): Retrofit {
        // okHttpClientBuilder
        val okHttpClientBuilder = OkHttpClient().newBuilder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .sslSocketFactory(createSSLSocketFactory(), createTrustManager())
            .addInterceptor {
                // set request token
                val request = it.request()
                val builder = request.newBuilder()

                // 接口里不应该包含context相关 ( sp / db ... )
                val token = tokenFactory()
                if (token.isNotEmpty()) {
                    builder.addHeader(TOKEN_KEY, token)
                }
                it.proceed(builder.build())
            }

        return Retrofit.Builder()
            .baseUrl(HOST)
            .client(okHttpClientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * get ServiceApi
     */
    fun <T> create(service: Class<T>): T = create().create(service)

    private fun createSSLSocketFactory(): SSLSocketFactory {
        val sc: SSLContext = SSLContext.getInstance("TLS")
        sc.init(null, arrayOf(createTrustManager()), SecureRandom())
        return sc.socketFactory
    }

    @SuppressLint("CustomX509TrustManager", "TrustAllX509TrustManager")
    private fun createTrustManager(): X509TrustManager {
        return object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {

            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {

            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return emptyArray()
            }
        }
    }
}
