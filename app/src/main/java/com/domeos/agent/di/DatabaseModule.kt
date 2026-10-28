package com.domeos.agent.di

import android.content.Context
import com.domeos.agent.data.local.DomeOSDatabase
import com.domeos.agent.data.local.dao.AppSettingsDao
import com.domeos.agent.data.local.dao.ChatMessageDao
import com.domeos.agent.data.local.dao.ChatSessionDao
import com.domeos.agent.data.local.dao.ProviderConfigDao
import com.domeos.agent.data.repository.RepositoryImpl.*
import com.domeos.agent.domain.repository.AppSettingsDao
import com.domeos.agent.domain.repository.ChatMessageRepository
import com.domeos.agent.domain.repository.ChatSessionRepository
import com.domeos.agent.domain.repository.ProviderRepository
import com.domeos.agent.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据库模块
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): DomeOSDatabase {
        return DomeOSDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideChatSessionDao(database: DomeOSDatabase): ChatSessionDao = database.chatSessionDao()

    @Provides
    @Singleton
    fun provideChatMessageDao(database: DomeOSDatabase): ChatMessageDao = database.chatMessageDao()

    @Provides
    @Singleton
    fun provideProviderConfigDao(database: DomeOSDatabase): ProviderConfigDao = database.providerConfigDao()

    @Provides
    @Singleton
    fun provideAppSettingsDao(database: DomeOSDatabase): AppSettingsDao = database.appSettingsDao()

    @Provides
    @Singleton
    fun provideChatSessionRepository(impl: ChatSessionRepositoryImpl): ChatSessionRepository = impl

    @Provides
    @Singleton
    fun provideChatMessageRepository(impl: ChatMessageRepositoryImpl): ChatMessageRepository = impl

    @Provides
    @Singleton
    fun provideProviderRepository(impl: ProviderRepositoryImpl): ProviderRepository = impl

    @Provides
    @Singleton
    fun provideSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository = impl
}