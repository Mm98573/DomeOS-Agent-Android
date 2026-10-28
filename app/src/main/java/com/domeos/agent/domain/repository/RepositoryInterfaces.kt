package com.domeos.agent.domain.repository

import com.domeos.agent.data.model.ChatMessage
import com.domeos.agent.data.model.ChatSession
import com.domeos.agent.data.model.ProviderConfig
import com.domeos.agent.data.model.AppSettings
import com.domeos.agent.data.model.SessionFilter
import kotlinx.coroutines.flow.Flow

/**
 * 会话仓库接口
 */
interface ChatSessionRepository {
    fun getAllSessions(): Flow<List<ChatSession>>
    fun getActiveSessions(): Flow<List<ChatSession>>
    fun getArchivedSessions(): Flow<List<ChatSession>>
    fun getPinnedSessions(): Flow<List<ChatSession>>
    fun getFilteredSessions(filter: SessionFilter): Flow<List<ChatSession>>
    fun getSession(id: Long): Flow<ChatSession?>
    fun getSessionSync(id: Long): ChatSession?
    fun insertSession(session: ChatSession): Long
    fun updateSession(session: ChatSession)
    fun deleteSession(id: Long)
    fun setArchived(id: Long, archived: Boolean)
    fun setPinned(id: Long, pinned: Boolean)
    fun setFavorite(id: Long, favorite: Boolean)
    fun updateTitle(id: Long, title: String)
    fun updateLastMessage(id: Long, preview: String, role: String)
    fun incrementTokens(id: Long, tokens: Long)
    fun searchSessions(query: String): Flow<List<ChatSession>>
    fun getAllTags(): List<String>
    fun getActiveCount(): Int
    fun getArchivedCount(): Int
}

/**
 * 消息仓库接口
 */
interface ChatMessageRepository {
    fun getMessages(chatId: Long): Flow<List<ChatMessage>>
    fun getMessagesSync(chatId: Long): List<ChatMessage>
    fun getLastStreamingMessage(chatId: Long): Flow<ChatMessage?>
    fun getLastStreamingMessageSync(chatId: Long): ChatMessage?
    fun getRecentMessages(chatId: Long, limit: Int): List<ChatMessage>
    fun getLastCompleteMessages(chatId: Long, limit: Int): List<ChatMessage>
    fun getToolMessages(chatId: Long): List<ChatMessage>
    fun insertMessage(message: ChatMessage): Long
    fun insertMessages(messages: List<ChatMessage>)
    fun updateMessage(message: ChatMessage)
    fun updateStreamingContent(id: Long, content: String, tokenCount: Int)
    fun markComplete(id: Long)
    fun setError(id: Long, error: String)
    fun updateToolCalls(id: Long, toolCalls: String)
    fun updateToolResults(id: Long, toolResults: String)
    fun updateReasoning(id: Long, reasoning: String)
    fun deleteMessage(id: Long)
    fun deleteMessagesByChatId(chatId: Long)
    fun deleteStreamingMessages(chatId: Long)
    fun getCount(chatId: Long): Int
    fun getTotalTokens(chatId: Long): Long
}

/**
 * 提供商仓库接口
 */
interface ProviderRepository {
    fun getAllProviders(): Flow<List<ProviderConfig>>
    fun getEnabledProviders(): Flow<List<ProviderConfig>>
    fun getProvider(id: String): Flow<ProviderConfig?>
    fun getProviderSync(id: String): ProviderConfig?
    fun insertProvider(provider: ProviderConfig): Long
    fun updateProvider(provider: ProviderConfig)
    fun deleteProvider(id: String)
    fun setEnabled(id: String, enabled: Boolean)
    fun updateApiKey(id: String, apiKey: String)
    fun updateDefaultModel(id: String, model: String)
    fun updateModels(id: String, models: String)
    fun updateSortOrder(id: String, order: Int)
    fun initializePresets()
}

/**
 * 设置仓库接口
 */
interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    fun getSettingsSync(): AppSettings?
    fun updateSettings(settings: AppSettings)
    fun updateThemeMode(mode: Int)
    fun updateLanguage(lang: String)
    fun updateDefaultProvider(id: String)
    fun updateDefaultModel(id: String)
    fun updateGlobalSystemPrompt(prompt: String)
    fun updateHotkeys(hotkeys: Map<String, String>)
    fun updateBiometricLock(enabled: Boolean)
    fun updateLastSync(time: Long)
}