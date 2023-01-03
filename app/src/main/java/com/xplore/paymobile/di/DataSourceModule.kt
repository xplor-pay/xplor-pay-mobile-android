package com.xplore.paymobile.di

import android.content.Context
import com.xplore.paymobile.data.datasource.EncryptedSharedPrefsDataSource
import com.xplore.paymobile.data.datasource.RemoteDataSource
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.data.remote.XplorApi
import com.xplore.paymobile.data.web.WebJsonConverter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

    @Singleton
    @Provides
    fun provideSharedPrefs(
        @ApplicationContext context: Context,
        webJsonConverter: WebJsonConverter
    ): SharedPreferencesDataSource = SharedPreferencesDataSource(context, webJsonConverter)

    @Singleton
    @Provides
    fun provideEncryptedPrefs(@ApplicationContext context: Context): EncryptedSharedPrefsDataSource =
        EncryptedSharedPrefsDataSource(context)

    @Singleton
    @Provides
    fun provideRemoteDataSource(
        xplorApi: XplorApi, sharedPreferencesDataSource: SharedPreferencesDataSource
    ): RemoteDataSource = RemoteDataSource(xplorApi, sharedPreferencesDataSource)
}