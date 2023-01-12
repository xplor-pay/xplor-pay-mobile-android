package com.xplore.paymobile.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.xplore.paymobile.data.datasource.RemoteDataSource
import com.xplore.paymobile.data.remote.XplorApi
import com.xplore.paymobile.data.web.JSBridge
import com.xplore.paymobile.data.web.WebJsonConverter
import com.xplore.paymobile.ui.merchantselection.search.merchant.MerchantPaginationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
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
    fun provideJsonConverter(): Gson = GsonBuilder().create()

    @Singleton
    @Provides
    fun provideWebJsonConverter(gson: Gson): WebJsonConverter = WebJsonConverter(gson)

    @Provides
    fun provideJsBridgeFlows(): JSBridge.JSBridgeFlows = JSBridge.JSBridgeFlows()

    @Singleton
    @Provides
    fun provideLoggingInterceptor(): Interceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return interceptor
    }

    @Singleton
    @Provides
    fun provideHttpClient(interceptor: Interceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        converterFactory: Converter.Factory
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(XplorApi.BASE_URL_BOARDING)
            .client(client)
            .addConverterFactory(converterFactory)
            .build()

    @Provides
    @Singleton
    fun provideXplorApi(retrofit: Retrofit): XplorApi =
        retrofit.create(XplorApi::class.java)

    @Provides
    fun providePaginationHelper(remoteDataSource: RemoteDataSource): MerchantPaginationHelper =
        MerchantPaginationHelper(remoteDataSource)
}