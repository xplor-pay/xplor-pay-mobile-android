package com.xplore.paymobile.di

import com.xplore.paymobile.interactiondetection.AppLifecycleCallbacks
import com.xplore.paymobile.interactiondetection.UserInteractionDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideUserInteractionDetector() =
        UserInteractionDetector()

    @Singleton
    @Provides
    fun provideAppLicycleCallbacks(userInteractionDetector: UserInteractionDetector) =
        AppLifecycleCallbacks(userInteractionDetector)
}