package com.domeos.agent.domain.usecase

import com.domeos.agent.data.model.ChatMessage
import com.domeos.agent.data.model.ChatSession
import com.domeos.agent.data.model.MessageRole
import com.domeos.agent.data.model.ProviderConfig
import com.domeos.agent.data.model.RequestConfig
import com.domeos.agent.data.remote.ApiClientFactory
import com.domeos.agent.data.remote.api.AnthropicApi
import com.domeos.agent.data.remote.api.GenericApi
import com.domeos.agent.data.remote.api.OpenAiApi
import com.domeos.agent.data.remote.dto.ApiDtos.*
import com.domeos.agent.domain.repository.ChatMessageRepository
import com.domeos.agent.domain.repository.ChatSessionRepository
import com.domeos.agent.domain.repository.ProviderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 聊天相关用例
 */
@Singleton
class ChatUseCases @Inject constructor(
    private val sessionRepository: ChatSessionRepository,
    private val messageRepository: ChatMessageRepository,
    private val providerRepository: ProviderRepository,
    private val apiClientFactory: ApiClientFactory
) {

    /** 创建新会话 */
    suspend fun createSession(
        title: String = "新建会话",
        systemPrompt: String = "",
        modelId: String = "",
        providerId: String = ""
    ): ChatSession {
        val session = ChatSession.createNew(title, systemPrompt, modelId, providerId)
        session.id = sessionRepository.insertSession(session)
        return session
    }

    /** 发送消息并获取流式响应 */
    fun sendMessageStream(
        session: ChatSession,
        userContent: String,
        config: RequestConfig
    ): Flow<ChatMessage> = flow {
        // 1. 保存用户消息
        val userMessage = ChatMessage.createUser(session.id, userContent)
        val userMessageId = messageRepository.insertMessage(userMessage)

        // 2. 更新会话最后消息预览
        sessionRepository.updateLastMessage(session.id, userContent, MessageRole.USER.value)
        sessionRepository.incrementTokens(session.id, userMessage.tokenCount.toLong())

        // 3. 创建助手流式消息
        val assistantMessage = ChatMessage.createAssistantStreaming(session.id)
        val assistantMessageId = messageRepository.insertMessage(assistantMessage)

        // 4. 构建上下文消息
        val contextMessages = buildContextMessages(session, config.maxContextMessages)

        // 5. 调用 API 流式响应
        val provider = providerRepository.getProviderSync(session.providerId)
            ?: throw IllegalStateException("Provider not found: ${session.providerId}")

        val apiKey = apiClientFactory.getDecryptedApiKey(provider)
        if (apiKey.isBlank()) {
            throw IllegalStateException("API Key not configured for provider: ${provider.name}")
        }

        val authHeader = apiClientFactory.buildAuthHeader(provider)

        try {
            when (provider.type) {
                com.domeos.agent.data.model.ProviderType.OPENAI,
                com.domeos.agent.data.model.ProviderType.OPENROUTER,
                com.domeos.agent.data.model.ProviderType.AZURE,
                com.domeos.agent.data.model.ProviderType.DEEPSEEK,
                com.domeos.agent.data.model.ProviderType.MOONSHOT,
                com.domeos.agent.data.model.ProviderType.ZHIPU,
                com.domeos.agent.data.model.ProviderType.BAIDU,
                com.domeos.agent.data.model.ProviderType.ALIYUN,
                com.domeos.agent.data.model.ProviderType.TENCENT,
                com.domeos.agent.data.model.ProviderType.CUSTOM,
                com.domeos.agent.data.model.ProviderType.OLLAMA -> {
                    val api = apiClientFactory.createOpenAiApi(provider) as OpenAiApi
                    streamOpenAiResponse(api, authHeader, session, contextMessages, config, assistantMessageId)
                }
                com.domeos.agent.data.model.ProviderType.ANTHROPIC -> {
                    val api = apiClientFactory.createAnthropicApi(provider) as AnthropicApi
                    streamAnthropicResponse(api, authHeader, session, contextMessages, config, assistantMessageId)
                }
                com.domeos.agent.data.model.ProviderType.GOOGLE -> {
                    val api = apiClientFactory.createGeminiApi(provider) as GeminiApi
                    streamGeminiResponse(api, authHeader, session, contextMessages, config, assistantMessageId)
                }
                else -> {
                    val api = apiClientFactory.createGenericApi(provider) as GenericApi
                    streamGenericResponse(api, authHeader, session, contextMessages, config, assistantMessageId)
                }
            }
        } catch (e: Exception) {
            messageRepository.setError(assistantMessageId, e.message ?: "Unknown error")
            emit(ChatMessage(
                id = assistantMessageId,
                chatId = session.id,
                role = MessageRole.ASSISTANT,
                content = "",
                error = e.message,
                isComplete = true,
                isStreaming = false,
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now()
            ))
        }
    }.flowOn(Dispatchers.IO)

    /** 停止生成 */
    fun stopGeneration(assistantMessageId: Long) {
        messageRepository.markComplete(assistantMessageId)
    }

    /** 重新生成最后一条助手消息 */
    suspend fun regenerateLastMessage(
        session: ChatSession,
        config: RequestConfig
    ): Flow<ChatMessage> = flow {
        // 删除最后一条助手消息及其后的所有消息
        val messages = messageRepository.getMessagesSync(session.id)
        var lastAssistantIndex = messages.lastIndexOfFirst { it.role == MessageRole.ASSISTANT }
        if (lastAssistantIndex >= 0) {
            val toDelete = messages.drop(lastAssistantIndex)
            toDelete.forEach { messageRepository.deleteMessage(it.id) }
        }
        // 获取最后一条用户消息
        val lastUserMessage = messages.lastOrNull { it.role == MessageRole.USER }
            ?: throw IllegalStateException("No user message to regenerate from")

        // 重新发送
        val flow = sendMessageStream(session, lastUserMessage.content, config)
        flow.collect { emit(it) }
    }.flowOn(Dispatchers.IO)

    /** 构建上下文消息 */
    private fun buildContextMessages(session: ChatSession, maxMessages: Int): List<ChatMessage> {
        val messages = messageRepository.getLastCompleteMessages(session.id, maxMessages)
        val contextMessages = mutableListOf<ChatMessage>()

        // 添加系统提示词
        val systemPrompt = session.systemPrompt.ifBlank { providerRepository.getProviderSync(session.providerId)?.getMetadata()?.getOrDefault("system_prompt", "") ?: "" }
        if (systemPrompt.isNotBlank()) {
            contextMessages.add(ChatMessage(
                role = "system",
                content = systemPrompt
            ))
        }

        // 添加历史消息（倒序后再正序）
        contextMessages.addAll(messages.reversed().map { msg ->
            when (msg.role) {
                MessageRole.USER -> ChatMessage(role = "user", content = msg.content)
                MessageRole.ASSISTANT -> ChatMessage(role = "assistant", content = msg.content)
                MessageRole.TOOL -> {
                    val results = msg.getToolResults()
                    results.map { ChatMessage(role = "tool", content = it.content, tool_call_id = it.toolCallId) }
                }
                else -> ChatMessage(role = msg.role.value, content = msg.content)
            }
        }.flatten())

        return contextMessages
    }

    /** 流式处理 OpenAI 兼容响应 */
    private fun streamOpenAiResponse(
        api: OpenAiApi,
        authHeader: String,
        session: ChatSession,
        contextMessages: List<ChatMessage>,
        config: RequestConfig,
        assistantMessageId: Long
    ) {
        val request = ChatRequest(
            model = session.modelId.ifBlank { config.model },
            messages = contextMessages.map { it.toApiMessage() },
            temperature = config.temperature,
            top_p = config.topP,
            top_k = config.topK,
            max_tokens = config.maxTokens,
            presence_penalty = config.presencePenalty,
            frequency_penalty = config.frequencyPenalty,
            stop = config.stopSequences.ifEmpty { null },
            stream = true,
            tools = if (config.enableTools) buildTools(session) else null,
            tool_choice = if (config.enableTools) "auto" else null
        )

        CoroutineScope(Dispatchers.IO).launch {
            val response = api.chatCompletionStream(authHeader, request)
            if (response.isSuccessful) {
                response.body()?.collect { line ->
                    parseOpenAiStreamLine(line, assistantMessageId, session.id)
                }
            } else {
                val error = response.errorBody()?.string() ?: "API Error: ${response.code()}"
                messageRepository.setError(assistantMessageId, error)
            }
        }
    }

    /** 解析 OpenAI 流式响应行 */
    private fun parseOpenAiStreamLine(line: String, messageId: Long, chatId: Long) {
        line.trim().let { trimmed ->
            if (trimmed.startsWith("data: ")) {
                val data = trimmed.substring(6)
                if (data == "[DONE]") {
                    messageRepository.markComplete(messageId)
                    return@let
                }
                try {
                    val chunk = Json.decodeFromString<ChatStreamResponse>(data)
                    chunk.choices.firstOrNull()?.let { choice ->
                        choice.delta.content?.let { content ->
                            if (content.isNotBlank()) {
                                messageRepository.updateStreamingContent(
                                    messageId,
                                    getMessageContent(messageId) + content,
                                    estimateTokens(content)
                                )
                            }
                        }
                        choice.delta.tool_calls?.forEach { toolCall ->
                            // 处理工具调用
                        }
                        choice.finish_reason?.let { reason ->
                            if (reason == "tool_calls") {
                                // 工具调用完成
                            } else if (reason == "stop" || reason == "length") {
                                messageRepository.markComplete(messageId)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // 忽略解析错误
                }
            }
        }
    }

    /** 获取当前消息内容 */
    private fun getMessageContent(messageId: Long): String {
        return messageRepository.getMessagesSync(chatId = 0).firstOrNull { it.id == messageId }?.content ?: ""
    }

    /** 估算 Token 数 */
    private fun estimateTokens(text: String): Int = (text.length / 3).coerceAtLeast(1)

    /** 构建工具定义 */
    private fun buildTools(session: ChatSession): List<Tool> {
        // TODO: 根据 enabledTools 构建工具列表
        return emptyList()
    }

    /** 流式处理 Anthropic 响应 */
    private fun streamAnthropicResponse(
        api: AnthropicApi,
        apiKey: String,
        session: ChatSession,
        contextMessages: List<ChatMessage>,
        config: RequestConfig,
        assistantMessageId: Long
    ) {
        // TODO: 实现 Anthropic 流式响应
    }

    /** 流式处理 Gemini 响应 */
    private fun streamGeminiResponse(
        api: GeminiApi,
        apiKey: String,
        session: ChatSession,
        contextMessages: List<ChatMessage>,
        config: RequestConfig,
        assistantMessageId: Long
    ) {
        // TODO: 实现 Gemini 流式响应
    }

    /** 流式处理通用响应 */
    private fun streamGenericResponse(
        api: GenericApi,
        authHeader: String,
        session: ChatSession,
        contextMessages: List<ChatMessage>,
        config: RequestConfig,
        assistantMessageId: Long
    ) {
        // TODO: 实现通用流式响应
    }
}

/** 扩展函数：转换为 API 消息格式 */
private fun ChatMessage.toApiMessage(): com.domeos.agent.data.remote.dto.ApiDtos.ChatMessage {
    return when (role) {
        MessageRole.USER -> com.domeos.agent.data.remote.dto.ApiDtos.ChatMessage.user(content)
        MessageRole.ASSISTANT -> com.domeos.agent.data.remote.dto.ApiDtos.ChatMessage.assistant(content)
        MessageRole.SYSTEM -> com.domeos.agent.data.remote.dto.ApiDtos.ChatMessage.system(content)
        MessageRole.TOOL -> {
            val toolResults = getToolResults()
            com.domeos.agent.data.remote.dto.ApiDtos.ChatMessage.tool(
                toolResults.firstOrNull()?.content ?: "",
                toolResults.firstOrNull()?.toolCallId ?: ""
            )
        }
        else -> com.domeos.agent.data.remote.dto.ApiDtos.ChatMessage(role = role.value, content = content)
    }
}