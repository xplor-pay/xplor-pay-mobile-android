package com.xplore.paymobile.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.xplore.paymobile.data.datasource.RemoteDataSource
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.data.remote.ClearentGatewayApi
import com.xplore.paymobile.data.remote.XplorApi
import com.xplore.paymobile.data.remote.XplorBoardingApi
import com.xplore.paymobile.data.web.JSBridge
import com.xplore.paymobile.data.web.VTRefreshManager
import com.xplore.paymobile.data.web.JsonConverterUtil
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
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkingModule {

    private const val XPLOR_API_NAME = "XplorApi"
    private const val XPLOR_BOARDING_API_NAME = "XplorBoardingApi"
    private const val CLEARENT_GATEWAY_API_NAME = "ClearentGatewayApi"

    @Singleton
    @Provides
    fun provideRetrofitJsonConverter(): Converter.Factory = GsonConverterFactory.create()

    @Singleton
    @Provides
    fun provideJsonConverter(): Gson = GsonBuilder().create()

    @Singleton
    @Provides
    fun provideWebJsonConverter(gson: Gson): JsonConverterUtil = JsonConverterUtil(gson)

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
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .build()

    @Provides
    @Singleton
    @Named(XPLOR_API_NAME)
    fun provideRetrofitXplorApi(
        client: OkHttpClient,
        converterFactory: Converter.Factory
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(XplorApi.BASE_URL)
            .client(client)
            .addConverterFactory(converterFactory)
            .build()

    @Provides
    @Singleton
    fun provideXplorApi(@Named(XPLOR_API_NAME) retrofit: Retrofit): XplorApi =
        retrofit.create(XplorApi::class.java)

    @Provides
    @Singleton
    @Named(XPLOR_BOARDING_API_NAME)
    fun provideRetrofitXplorBoardingApi(
        client: OkHttpClient,
        converterFactory: Converter.Factory
    ): Retrofit = Retrofit.Builder()
        .baseUrl(XplorBoardingApi.BASE_URL)
        .client(client)
        .addConverterFactory(converterFactory)
        .build()

    @Provides
    @Singleton
    fun provideXplorBoardingApi(@Named(XPLOR_BOARDING_API_NAME) retrofit: Retrofit): XplorBoardingApi =
        retrofit.create(XplorBoardingApi::class.java)

    @Provides
    @Singleton
    @Named(CLEARENT_GATEWAY_API_NAME)
    fun provideRetrofitClearentGatewayApi(
        client: OkHttpClient,
        converterFactory: Converter.Factory
    ): Retrofit = Retrofit.Builder()
        .baseUrl(ClearentGatewayApi.BASE_URL)
        .client(client)
        .addConverterFactory(converterFactory)
        .build()

    @Provides
    @Singleton
    fun provideClearentGatewayApi(@Named(CLEARENT_GATEWAY_API_NAME) retrofit: Retrofit): ClearentGatewayApi =
        retrofit.create(ClearentGatewayApi::class.java)

    @Provides
    fun providePaginationHelper(remoteDataSource: RemoteDataSource): MerchantPaginationHelper =
        MerchantPaginationHelper(remoteDataSource)

    @Provides
    @Singleton
    fun provideVTTimerManager(
        remoteDataSource: RemoteDataSource,
        sharedPreferencesDataSource: SharedPreferencesDataSource
    ): VTRefreshManager = VTRefreshManager(remoteDataSource, sharedPreferencesDataSource)
}