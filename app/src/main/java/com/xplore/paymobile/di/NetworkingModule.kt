package com.xplore.paymobile.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.xplore.paymobile.data.remote.ClearentGatewayApi
import com.xplore.paymobile.data.remote.XplorApi
import com.xplore.paymobile.data.remote.XplorBoardingApi
import com.xplore.paymobile.data.web.JSBridge
import com.xplore.paymobile.data.web.WebJsonConverter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.simpleframework.xml.convert.AnnotationStrategy
import org.simpleframework.xml.core.Persister
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkingModule {

    @Singleton
    @Provides
    @JSON
    fun provideRetrofitJsonConverter(): Converter.Factory = GsonConverterFactory.create()

    @Singleton
    @Provides
    @XML
    fun provideRetrofitXMLConverter(): Converter.Factory =
        SimpleXmlConverterFactory.createNonStrict(
            Persister(
                AnnotationStrategy()
            )
        )

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
    @Named("Xplor")
    fun provideRetrofitXplorApi(
        client: OkHttpClient,
        @JSON converterFactory: Converter.Factory
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(XplorApi.BASE_URL)
            .client(client)
            .addConverterFactory(converterFactory)
            .build()

    @Provides
    @Singleton
    fun provideXplorApi(@Named("Xplor") retrofit: Retrofit): XplorApi =
        retrofit.create(XplorApi::class.java)

    @Provides
    @Singleton
    @Named("XplorBoarding")
    fun provideRetrofitXplorBoardingApi(
        client: OkHttpClient,
        @JSON converterFactory: Converter.Factory
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(XplorBoardingApi.BASE_URL)
            .client(client)
            .addConverterFactory(converterFactory)
            .build()

    @Provides
    @Singleton
    fun provideXplorBoardingApi(@Named("XplorBoarding") retrofit: Retrofit): XplorBoardingApi =
        retrofit.create(XplorBoardingApi::class.java)

    @Provides
    @Singleton
    @Named("ClearentGateway")
    fun provideRetrofitClearentGatewayApi(
        client: OkHttpClient,
        @JSON converterFactory: Converter.Factory
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(ClearentGatewayApi.BASE_URL)
            .client(client)
            .addConverterFactory(converterFactory)
            .build()

    @Provides
    @Singleton
    fun provideClearentGatewayApi(@Named("ClearentGateway") retrofit: Retrofit): ClearentGatewayApi =
        retrofit.create(ClearentGatewayApi::class.java)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class JSON

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class XML