package com.domeos.agent.di

import com.domeos.agent.domain.usecase.ChatUseCases
import com.domeos.agent.domain.usecase.ProviderUseCases
import com.domeos.agent.domain.usecase.SessionUseCases
import com.domeos.agent.domain.usecase.SettingsUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 用例模块
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideChatUseCases(chatUseCases: ChatUseCases): ChatUseCases = chatUseCases

    @Provides
    @Singleton
    fun provideSessionUseCases(sessionUseCases: SessionUseCases): SessionUseCases = sessionUseCases

    @Provides
    @Singleton
    fun provideProviderUseCases(providerUseCases: ProviderUseCases): ProviderUseCases = providerUseCases

    @Provides
    @Singleton
    fun provideSettingsUseCases(settingsUseCases: SettingsUseCases): SettingsUseCases = settingsUseCases
}