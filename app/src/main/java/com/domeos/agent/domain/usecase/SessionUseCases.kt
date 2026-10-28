package com.domeos.agent.domain.usecase

import com.domeos.agent.data.model.ChatSession
import com.domeos.agent.data.model.ProviderConfig
import com.domeos.agent.data.model.SessionFilter
import com.domeos.agent.domain.repository.ChatSessionRepository
import com.domeos.agent.domain.repository.ProviderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 会话管理用例
 */
@Singleton
class SessionUseCases @Inject constructor(
    private val sessionRepository: ChatSessionRepository,
    private val providerRepository: ProviderRepository
) {

    /** 获取所有会话 */
    fun getAllSessions(): Flow<List<ChatSession>> = sessionRepository.getAllSessions()

    /** 获取活跃会话 */
    fun getActiveSessions(): Flow<List<ChatSession>> = sessionRepository.getActiveSessions()

    /** 获取归档会话 */
    fun getArchivedSessions(): Flow<List<ChatSession>> = sessionRepository.getArchivedSessions()

    /** 获取置顶会话 */
    fun getPinnedSessions(): Flow<List<ChatSession>> = sessionRepository.getPinnedSessions()

    /** 获取过滤后的会话 */
    fun getFilteredSessions(filter: SessionFilter): Flow<List<ChatSession>> = sessionRepository.getFilteredSessions(filter)

    /** 获取单个会话 */
    fun getSession(id: Long): Flow<ChatSession?> = sessionRepository.getSession(id)

    /** 获取单个会话（同步） */
    fun getSessionSync(id: Long): ChatSession? = sessionRepository.getSessionSync(id)

    /** 更新会话 */
    fun updateSession(session: ChatSession) = sessionRepository.updateSession(session)

    /** 删除会话 */
    fun deleteSession(id: Long) = sessionRepository.deleteSession(id)

    /** 归档/取消归档会话 */
    fun setArchived(id: Long, archived: Boolean) = sessionRepository.setArchived(id, archived)

    /** 置顶/取消置顶会话 */
    fun setPinned(id: Long, pinned: Boolean) = sessionRepository.setPinned(id, pinned)

    /** 收藏/取消收藏会话 */
    fun setFavorite(id: Long, favorite: Boolean) = sessionRepository.setFavorite(id, favorite)

    /** 重命名会话 */
    fun renameSession(id: Long, title: String) = sessionRepository.updateTitle(id, title)

    /** 搜索会话 */
    fun searchSessions(query: String): Flow<List<ChatSession>> = sessionRepository.searchSessions(query)

    /** 获取所有标签 */
    fun getAllTags(): List<String> = sessionRepository.getAllTags()

    /** 获取活跃会话数 */
    fun getActiveCount(): Int = sessionRepository.getActiveCount()

    /** 获取归档会话数 */
    fun getArchivedCount(): Int = sessionRepository.getArchivedCount()

    /** 创建分支会话 */
    fun createBranchSession(parentId: Long, title: String): ChatSession {
        val parent = sessionRepository.getSessionSync(parentId)
            ?: throw IllegalArgumentException("Parent session not found: $parentId")

        val branch = ChatSession.createNew(
            title = title,
            systemPrompt = parent.systemPrompt,
            modelId = parent.modelId,
            providerId = parent.providerId
        ).apply {
            parentId = parentId
            temperature = parent.temperature
            maxTokens = parent.maxTokens
            enableStreaming = parent.enableStreaming
            enableTools = parent.enableTools
        }

        branch.id = sessionRepository.insertSession(branch)
        return branch
    }

    /** 复制会话 */
    fun duplicateSession(id: Long): ChatSession {
        val original = sessionRepository.getSessionSync(id)
            ?: throw IllegalArgumentException("Session not found: $id")

        val copy = ChatSession.createNew(
            title = "${original.title} (副本)",
            systemPrompt = original.systemPrompt,
            modelId = original.modelId,
            providerId = original.providerId
        ).apply {
            temperature = original.temperature
            maxTokens = original.maxTokens
            enableStreaming = original.enableStreaming
            enableTools = original.enableTools
            tags = original.tags
        }

        copy.id = sessionRepository.insertSession(copy)
        return copy
    }
}

/**
 * 提供商管理用例
 */
@Singleton
class ProviderUseCases @Inject constructor(
    private val providerRepository: ProviderRepository
) {

    fun getAllProviders(): Flow<List<ProviderConfig>> = providerRepository.getAllProviders()

    fun getEnabledProviders(): Flow<List<ProviderConfig>> = providerRepository.getEnabledProviders()

    fun getProvider(id: String): Flow<ProviderConfig?> = providerRepository.getProvider(id)

    fun getProviderSync(id: String): ProviderConfig? = providerRepository.getProviderSync(id)

    fun addProvider(provider: ProviderConfig): Long = providerRepository.insertProvider(provider)

    fun updateProvider(provider: ProviderConfig) = providerRepository.updateProvider(provider)

    fun deleteProvider(id: String) = providerRepository.deleteProvider(id)

    fun setEnabled(id: String, enabled: Boolean) = providerRepository.setEnabled(id, enabled)

    fun updateApiKey(id: String, apiKey: String) = providerRepository.updateApiKey(id, apiKey)

    fun updateDefaultModel(id: String, model: String) = providerRepository.updateDefaultModel(id, model)

    fun updateModels(id: String, models: String) = providerRepository.updateModels(id, models)

    fun reorderProviders(providers: List<ProviderConfig>) {
        providers.forEachIndexed { index, provider ->
            provider.sortOrder = index
            providerRepository.updateProvider(provider)
        }
    }

    fun initializePresets() = providerRepository.initializePresets()
}