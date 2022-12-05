package com.xplore.paymobile.di

import com.xplore.paymobile.api.XplorApi
import com.xplore.paymobile.datasource.RemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkingModule {

    @Singleton
    @Provides
    fun provideConverterFactory(): Converter.Factory = GsonConverterFactory.create()

    @Singleton
    @Provides
    fun provideInterceptors(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return interceptor
    }

    @Singleton
    @Provides
    fun provideHttpClient(interceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(converterFactory: Converter.Factory): Retrofit =
        Retrofit.Builder()
            .baseUrl(XplorApi.BASE_URL)
            .addConverterFactory(converterFactory)
            .build()

    @Provides
    @Singleton
    fun provideRestaurantApi(retrofit: Retrofit): XplorApi =
        retrofit.create(XplorApi::class.java)

    @Provides
    @Singleton
    fun providesRemoteDataSource(xplorApi: XplorApi): RemoteDataSource = RemoteDataSource(xplorApi)
}