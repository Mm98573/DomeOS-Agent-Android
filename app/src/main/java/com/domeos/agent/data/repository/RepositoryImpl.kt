package com.domeos.agent.data.repository

import com.domeos.agent.data.local.DomeOSDatabase
import com.domeos.agent.data.local.dao.ChatMessageDao
import com.domeos.agent.data.local.dao.ChatSessionDao
import com.domeos.agent.data.model.ChatMessage
import com.domeos.agent.data.model.ChatSession
import com.domeos.agent.data.model.MessageRole
import com.domeos.agent.data.model.SessionFilter
import com.domeos.agent.data.model.SessionSortOrder
import com.domeos.agent.domain.repository.ChatMessageRepository
import com.domeos.agent.domain.repository.ChatSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 会话仓库实现
 */
@Singleton
class ChatSessionRepositoryImpl @Inject constructor(
    private val database: DomeOSDatabase
) : ChatSessionRepository {

    private val dao: ChatSessionDao = database.chatSessionDao()

    override fun getAllSessions(): Flow<List<ChatSession>> = dao.getRootSessions()

    override fun getActiveSessions(): Flow<List<ChatSession>> = dao.getActiveSessions()

    override fun getArchivedSessions(): Flow<List<ChatSession>> = dao.getArchivedSessions()

    override fun getPinnedSessions(): Flow<List<ChatSession>> = dao.getPinnedSessions()

    override fun getFilteredSessions(filter: SessionFilter): Flow<List<ChatSession>> {
        return dao.getFiltered(
            showArchived = filter.showArchived,
            showPinnedOnly = filter.showPinnedOnly,
            providerId = filter.providerId,
            query = filter.query,
            sortOrder = filter.sortOrder.ordinal
        )
    }

    override fun getSession(id: Long): Flow<ChatSession?> = dao.getById(id)

    override fun getSessionSync(id: Long): ChatSession? = dao.getByIdSync(id)

    override fun insertSession(session: ChatSession): Long = dao.insert(session)

    override fun updateSession(session: ChatSession) {
        dao.update(session)
    }

    override fun deleteSession(id: Long) {
        dao.deleteById(id)
    }

    override fun setArchived(id: Long, archived: Boolean) {
        dao.setArchived(id, archived)
    }

    override fun setPinned(id: Long, pinned: Boolean) {
        dao.setPinned(id, pinned)
    }

    override fun setFavorite(id: Long, favorite: Boolean) {
        dao.setFavorite(id, favorite)
    }

    override fun updateTitle(id: Long, title: String) {
        dao.updateTitle(id, title, Clock.System.now().epochMilliseconds)
    }

    override fun updateLastMessage(id: Long, preview: String, role: String) {
        dao.updateLastMessage(id, preview, role, Clock.System.now().epochMilliseconds)
    }

    override fun incrementTokens(id: Long, tokens: Long) {
        dao.incrementTokens(id, tokens)
    }

    override fun searchSessions(query: String): Flow<List<ChatSession>> {
        return dao.searchByTitle(query)
    }

    override fun getAllTags(): List<String> = dao.getAllTags()

    override fun getActiveCount(): Int = dao.getActiveCount()

    override fun getArchivedCount(): Int = dao.getArchivedCount()
}

/**
 * 消息仓库实现
 */
@Singleton
class ChatMessageRepositoryImpl @Inject constructor(
    private val database: DomeOSDatabase
) : ChatMessageRepository {

    private val dao: ChatMessageDao = database.chatMessageDao()

    override fun getMessages(chatId: Long): Flow<List<ChatMessage>> = dao.getByChatId(chatId)

    override fun getMessagesSync(chatId: Long): List<ChatMessage> = dao.getByChatIdSync(chatId)

    override fun getLastStreamingMessage(chatId: Long): Flow<ChatMessage?> = dao.getLastStreamingMessage(chatId)

    override fun getLastStreamingMessageSync(chatId: Long): ChatMessage? = dao.getLastStreamingMessageSync(chatId)

    override fun getRecentMessages(chatId: Long, limit: Int): List<ChatMessage> = dao.getRecentMessages(chatId, limit)

    override fun getLastCompleteMessages(chatId: Long, limit: Int): List<ChatMessage> = dao.getLastCompleteMessages(chatId, limit)

    override fun getToolMessages(chatId: Long): List<ChatMessage> = dao.getToolMessages(chatId)

    override fun insertMessage(message: ChatMessage): Long = dao.insert(message)

    override fun insertMessages(messages: List<ChatMessage>) {
        dao.insertAll(messages)
    }

    override fun updateMessage(message: ChatMessage) {
        dao.update(message)
    }

    override fun updateStreamingContent(id: Long, content: String, tokenCount: Int) {
        dao.updateStreamingContent(id, content, Clock.System.now().epochMilliseconds, tokenCount)
    }

    override fun markComplete(id: Long) {
        dao.markComplete(id, Clock.System.now().epochMilliseconds)
    }

    override fun setError(id: Long, error: String) {
        dao.setError(id, error, Clock.System.now().epochMilliseconds)
    }

    override fun updateToolCalls(id: Long, toolCalls: String) {
        dao.updateToolCalls(id, toolCalls, Clock.System.now().epochMilliseconds)
    }

    override fun updateToolResults(id: Long, toolResults: String) {
        dao.updateToolResults(id, toolResults, Clock.System.now().epochMilliseconds)
    }

    override fun updateReasoning(id: Long, reasoning: String) {
        dao.updateReasoning(id, reasoning, Clock.System.now().epochMilliseconds)
    }

    override fun deleteMessage(id: Long) {
        dao.deleteById(id)
    }

    override fun deleteMessagesByChatId(chatId: Long) {
        dao.deleteByChatId(chatId)
    }

    override fun deleteStreamingMessages(chatId: Long) {
        dao.deleteStreamingMessages(chatId)
    }

    override fun getCount(chatId: Long): Int = dao.getCountByChatId(chatId)

    override fun getTotalTokens(chatId: Long): Long = dao.getTotalTokens(chatId) ?: 0
}

/**
 * 提供商仓库实现
 */
@Singleton
class ProviderRepositoryImpl @Inject constructor(
    private val database: DomeOSDatabase
) : com.domeos.agent.domain.repository.ProviderRepository {

    private val dao: com.domeos.agent.data.local.dao.ProviderConfigDao = database.providerConfigDao()

    override fun getAllProviders(): Flow<List<com.domeos.agent.data.model.ProviderConfig>> = dao.getAllProviders()

    override fun getEnabledProviders(): Flow<List<com.domeos.agent.data.model.ProviderConfig>> = dao.getEnabledProviders()

    override fun getProvider(id: String): Flow<com.domeos.agent.data.model.ProviderConfig?> = dao.getById(id)

    override fun getProviderSync(id: String): com.domeos.agent.data.model.ProviderConfig? = dao.getByIdSync(id)

    override fun insertProvider(provider: com.domeos.agent.data.model.ProviderConfig): Long = dao.insert(provider)

    override fun updateProvider(provider: com.domeos.agent.data.model.ProviderConfig) {
        dao.update(provider)
    }

    override fun deleteProvider(id: String) {
        dao.deleteById(id)
    }

    override fun setEnabled(id: String, enabled: Boolean) {
        dao.setEnabled(id, enabled)
    }

    override fun updateApiKey(id: String, apiKey: String) {
        dao.updateApiKey(id, apiKey, Clock.System.now().epochMilliseconds)
    }

    override fun updateDefaultModel(id: String, model: String) {
        dao.updateDefaultModel(id, model, Clock.System.now().epochMilliseconds)
    }

    override fun updateModels(id: String, models: String) {
        dao.updateModels(id, models, Clock.System.now().epochMilliseconds)
    }

    override fun updateSortOrder(id: String, order: Int) {
        dao.updateSortOrder(id, order)
    }

    override fun initializePresets() {
        // 在后台线程初始化预设提供商
    }
}

/**
 * 设置仓库实现
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val database: DomeOSDatabase
) : com.domeos.agent.domain.repository.SettingsRepository {

    private val dao: com.domeos.agent.data.local.dao.AppSettingsDao = database.appSettingsDao()

    override fun getSettings(): Flow<com.domeos.agent.data.model.AppSettings> = dao.getSettings()

    override fun getSettingsSync(): com.domeos.agent.data.model.AppSettings? = dao.getSettingsSync()

    override fun updateSettings(settings: com.domeos.agent.data.model.AppSettings) {
        dao.update(settings)
    }

    override fun updateThemeMode(mode: Int) {
        dao.updateThemeMode(mode, Clock.System.now().epochMilliseconds)
    }

    override fun updateLanguage(lang: String) {
        dao.updateLanguage(lang, Clock.System.now().epochMilliseconds)
    }

    override fun updateDefaultProvider(id: String) {
        dao.updateDefaultProvider(id, Clock.System.now().epochMilliseconds)
    }

    override fun updateDefaultModel(id: String) {
        dao.updateDefaultModel(id, Clock.System.now().epochMilliseconds)
    }

    override fun updateGlobalSystemPrompt(prompt: String) {
        dao.updateGlobalSystemPrompt(prompt, Clock.System.now().epochMilliseconds)
    }

    override fun updateHotkeys(hotkeys: Map<String, String>) {
        dao.updateHotkeys(com.domeos.agent.data.model.AppSettings.DEFAULT_SYSTEM_PROMPT, Clock.System.now().epochMilliseconds)
    }

    override fun updateBiometricLock(enabled: Boolean) {
        dao.updateBiometricLock(enabled, Clock.System.now().epochMilliseconds)
    }

    override fun updateLastSync(time: Long) {
        dao.updateLastSync(time, Clock.System.now().epochMilliseconds)
    }
}