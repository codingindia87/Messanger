package com.codingindia.messanger.core.module

import com.codingindia.messanger.core.api.CallingAPI
import com.codingindia.messanger.core.api.SendMessageApi
import com.codingindia.messanger.core.di.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {

    companion object{
        private const val BASE_URL = "https://fcm.googleapis.com"
    }

    @Singleton
    @Provides
    fun providesRetrofitBuilder(): Retrofit.Builder {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder().addInterceptor(authInterceptor).build()
    }


    @Singleton
    @Provides
    fun providesSendMessage(builder: Retrofit.Builder, okHttpClient: OkHttpClient): SendMessageApi {
        return builder.client(okHttpClient).build().create(SendMessageApi::class.java)
    }

    @Singleton
    @Provides
    fun providesCalling(builder: Retrofit.Builder, okHttpClient: OkHttpClient): CallingAPI{
        return builder.client(okHttpClient).build().create(CallingAPI::class.java)
    }

}