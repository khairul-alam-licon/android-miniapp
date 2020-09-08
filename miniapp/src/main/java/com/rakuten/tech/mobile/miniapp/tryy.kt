package com.rakuten.tech.mobile.miniapp

import okhttp3.Interceptor
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer
import okio.ByteString
import java.io.IOException

// https://github.com/square/okhttp/issues/259
// https://github.com/francisco-sanchez-molina/react-native-fetch-blob/blob/4bbca78c4c1bc86283bc225c7bc225ee9620e2a9/src/android/src/main/java/com/RNFetchBlob/RNFetchBlobBody.java
class DummyInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .removeHeader("Content-Length")
        val request = requestBuilder.build()

        val buffer = Buffer()
        original.body()?.writeTo(buffer)
        val byteString = buffer.snapshot()
        val fixedLength = RequestBody.create(original.body()?.contentType(), byteString)
        return chain.proceed(request)
    }
}
