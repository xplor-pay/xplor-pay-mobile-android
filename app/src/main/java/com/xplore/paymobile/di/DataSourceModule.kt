package com.xplore.paymobile.di

import android.content.Context
import com.xplore.paymobile.util.EncryptedSharedPrefsDataSource
import com.xplore.paymobile.util.SharedPreferencesDataSource
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
    fun provideSharedPrefs(@ApplicationContext context: Context) =
        SharedPreferencesDataSource(context)

    @Singleton
    @Provides
    fun provideEncryptedPrefs(@ApplicationContext context: Context) =
        EncryptedSharedPrefsDataSource(context)
}