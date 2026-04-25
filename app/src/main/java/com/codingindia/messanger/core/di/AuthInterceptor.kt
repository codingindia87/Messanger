package com.codingindia.messanger.core.di

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        val token = AccessToken.getAccessToken()

        request.addHeader("Authorization", "Bearer $token")

        return chain.proceed(request.build())

    }
}