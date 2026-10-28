package com.domeos.agent.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.HiltViewModel
import androidx.lifecycle.viewmodel.viewModelScope
import com.domeos.agent.data.model.ChatMessage
import com.domeos.agent.data.model.ChatSession
import com.domeos.agent.data.model.MessageRole
import com.domeos.agent.data.model.RequestConfig
import com.domeos.agent.domain.usecase.ChatUseCases
import com.domeos.agent.domain.usecase.SessionUseCases
import com.domeos.agent.domain.usecase.SettingsUseCases
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 主界面 ViewModel
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val chatUseCases: ChatUseCases,
    private val sessionUseCases: SessionUseCases,
    private val settingsUseCases: SettingsUseCases
) : androidx.lifecycle.ViewModel() {

    // 当前会话
    private val _currentSession = MutableStateFlow<ChatSession?>(null)
    val currentSession: StateFlow<ChatSession?> = _currentSession.asStateFlow()

    // 消息列表
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // 输入文本
    var inputText by mutableStateOf("")
        private set

    // 是否正在流式生成
    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    // 当前流式消息 ID
    private val _currentStreamingMessageId = MutableStateFlow<Long?>(null)
    val currentStreamingMessageId: StateFlow<Long?> = _currentStreamingMessageId.asStateFlow()

    // 请求配置
    private val _requestConfig = MutableStateFlow(RequestConfig.default(""))
    val requestConfig: StateFlow<RequestConfig> = _requestConfig.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 错误信息
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** 初始化：创建或加载会话 */
    fun initialize(sessionId: Long? = null) {
        if (sessionId != null) {
            loadSession(sessionId)
        } else {
            createNewSession()
        }
    }

    /** 加载会话 */
    private fun loadSession(sessionId: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            sessionUseCases.getSession(sessionId).collect { session ->
                session?.let {
                    _currentSession.value = it
                    loadMessages(it.id)
                    updateRequestConfig(it)
                }
                _isLoading.value = false
            }
        }
    }

    /** 创建新会话 */
    fun createNewSession() {
        viewModelScope.launch {
            val settings = settingsUseCases.getSettingsSync()
            val session = chatUseCases.createSession(
                title = "新建会话",
                systemPrompt = settings?.globalSystemPrompt ?: "",
                modelId = settings?.defaultModelId ?: "",
                providerId = settings?.defaultProviderId ?: ""
            )
            _currentSession.value = session
            _messages.value = emptyList()
            updateRequestConfig(session)
        }
    }

    /** 加载消息 */
    private fun loadMessages(chatId: Long) {
        viewModelScope.launch {
            sessionUseCases.sessionRepository.getMessages(chatId).collect { msgs ->
                _messages.value = msgs
            }
        }
    }

    /** 更新请求配置 */
    private fun updateRequestConfig(session: ChatSession) {
        val settings = settingsUseCases.getSettingsSync()
        _requestConfig.value = RequestConfig(
            model = session.modelId.ifBlank { settings?.defaultModelId ?: "" },
            temperature = session.temperature,
            maxTokens = session.maxTokens,
            enableStreaming = session.enableStreaming,
            enableTools = session.enableTools
        )
    }

    /** 发送消息 */
    fun sendMessage() {
        val text = inputText.trim()
        if (text.isBlank() || _isStreaming.value) return

        val session = _currentSession.value ?: return
        _inputText.value = ""
        _isStreaming.value = true

        // 添加用户消息到 UI
        val userMessage = ChatMessage.createUser(session.id, text)
        _messages.value = _messages.value + userMessage

        // 流式获取响应
        viewModelScope.launch {
            val flow = chatUseCases.sendMessageStream(session, text, _requestConfig.value)
            var assistantMessage: ChatMessage? = null

            flow.collect { message ->
                when (message) {
                    is ChatMessage -> {
                        if (message.id == 0) {
                            // 新的流式消息
                            assistantMessage = message.copy(id = _currentStreamingMessageId.value ?: 0)
                            _messages.value = _messages.value + assistantMessage!!
                            _currentStreamingMessageId.value = assistantMessage!!.id
                        } else {
                            // 更新现有消息
                            val index = _messages.value.indexOfFirst { it.id == message.id }
                            if (index >= 0) {
                                val updated = _messages.value.toMutableList()
                                updated[index] = message
                                _messages.value = updated
                            }
                        }

                        if (message.isComplete) {
                            _isStreaming.value = false
                            _currentStreamingMessageId.value = null
                            // 更新会话最后消息
                            sessionUseCases.updateLastMessage(session.id, message.content, message.role.value)
                            sessionUseCases.incrementTokens(session.id, message.tokenCount.toLong())
                            // 自动重命名会话（首次对话）
                            if (session.title == "新建会话" && _messages.value.size <= 2) {
                                autoRenameSession(session, text)
                            }
                        }
                    }
                }
            }
        }
    }

    /** 自动重命名会话 */
    private fun autoRenameSession(session: ChatSession, firstMessage: String) {
        viewModelScope.launch {
            // 使用第一条用户消息的前 20 字符作为标题
            val title = firstMessage.take(20).trim()
            if (title.isNotBlank()) {
                sessionUseCases.renameSession(session.id, title)
                _currentSession.value = session.copy(title = title)
            }
        }
    }

    /** 停止生成 */
    fun stopGeneration() {
        _currentStreamingMessageId.value?.let { id ->
            chatUseCases.stopGeneration(id)
            _isStreaming.value = false
            _currentStreamingMessageId.value = null
        }
    }

    /** 重新生成 */
    fun regenerate() {
        val session = _currentSession.value ?: return
        val lastUserMessage = _messages.value.lastOrNull { it.role == MessageRole.USER }
            ?: return

        // 删除最后一条助手消息
        val lastAssistantIndex = _messages.value.lastIndexOfFirst { it.role == MessageRole.ASSISTANT }
        if (lastAssistantIndex >= 0) {
            _messages.value = _messages.value.take(lastAssistantIndex)
        }

        sendMessage()
    }

    /** 更新输入文本 */
    fun updateInputText(text: String) {
        _inputText.value = text
    }

    /** 切换会话 */
    fun switchSession(session: ChatSession) {
        _currentSession.value = session
        _messages.value = emptyList()
        _isStreaming.value = false
        _currentStreamingMessageId.value = null
        loadMessages(session.id)
        updateRequestConfig(session)
    }

    /** 删除当前会话 */
    fun deleteCurrentSession() {
        _currentSession.value?.let { session ->
            sessionUseCases.deleteSession(session.id)
            _currentSession.value = null
            _messages.value = emptyList()
        }
    }

    /** 归档当前会话 */
    fun archiveCurrentSession() {
        _currentSession.value?.let { session ->
            sessionUseCases.setArchived(session.id, true)
            _currentSession.value = null
            _messages.value = emptyList()
        }
    }

    /** 置顶当前会话 */
    fun togglePinCurrentSession() {
        _currentSession.value?.let { session ->
            val newPinned = !session.isPinned
            sessionUseCases.setPinned(session.id, newPinned)
            _currentSession.value = session.copy(isPinned = newPinned)
        }
    }

    /** 更新请求配置 */
    fun updateRequestConfig(config: RequestConfig) {
        _requestConfig.value = config
    }
}