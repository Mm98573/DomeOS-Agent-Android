package com.domeos.agent.di

import com.domeos.agent.data.remote.ApiClientFactory
import com.domeos.agent.util.EncryptionUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 网络模块
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideApiClientFactory(): ApiClientFactory = ApiClientFactory()

    @Provides
    @Singleton
    fun provideEncryptionUtil(@dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context): EncryptionUtil {
        return EncryptionUtil(context)
    }
}