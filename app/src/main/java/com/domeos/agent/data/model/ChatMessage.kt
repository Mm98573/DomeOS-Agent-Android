package com.domeos.agent.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.domeos.agent.data.local.converters.Converters
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 聊天消息实体
 * 对应 WinUI 版本中的 Message 数据结构
 */
@Entity(tableName = "messages")
@Serializable
@TypeConverters(Converters::class)
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    /** 所属会话 ID */
    var chatId: Long = 0,

    /** 消息角色 */
    var role: MessageRole = MessageRole.USER,

    /** 消息内容（支持 Markdown） */
    var content: String = "",

    /** 工具调用信息（JSON 存储） */
    var toolCalls: String? = null,

    /** 工具调用结果 */
    var toolResults: String? = null,

    /** 思考过程（用于推理模型） */
    var reasoning: String? = null,

    /** 创建时间 */
    var createdAt: Instant = Instant.now(),

    /** 更新时间（用于流式更新） */
    var updatedAt: Instant = Instant.now(),

    /** 是否正在流式生成中 */
    var isStreaming: Boolean = false,

    /** 是否生成完成 */
    var isComplete: Boolean = true,

    /** 错误信息 */
    var error: String? = null,

    /** Token 统计 */
    var tokenCount: Int = 0,

    /** 元数据（JSON） */
    var metadata: String? = null
) {
    /** 获取解析后的工具调用列表 */
    fun getToolCalls(): List<ToolCall> {
        return toolCalls?.let { Json.decodeFromString<List<ToolCall>>(it) } ?: emptyList()
    }

    /** 获取解析后的工具结果列表 */
    fun getToolResults(): List<ToolResult> {
        return toolResults?.let { Json.decodeFromString<List<ToolResult>>(it) } ?: emptyList()
    }

    /** 设置工具调用 */
    fun setToolCalls(calls: List<ToolCall>) {
        toolCalls = Json.encodeToString(calls)
    }

    /** 设置工具结果 */
    fun setToolResults(results: List<ToolResult>) {
        toolResults = Json.encodeToString(results)
    }

    /** 获取元数据 Map */
    fun getMetadata(): Map<String, String> {
        return metadata?.let { Json.decodeFromString<Map<String, String>>(it) } ?: emptyMap()
    }

    /** 设置元数据 */
    fun setMetadata(data: Map<String, String>) {
        metadata = Json.encodeToString(data)
    }

    companion object {
        /** 创建用户消息 */
        fun createUser(chatId: Long, content: String): ChatMessage {
            return ChatMessage(
                chatId = chatId,
                role = MessageRole.USER,
                content = content,
                isComplete = true
            )
        }

        /** 创建助手消息（流式） */
        fun createAssistantStreaming(chatId: Long): ChatMessage {
            return ChatMessage(
                chatId = chatId,
                role = MessageRole.ASSISTANT,
                content = "",
                isStreaming = true,
                isComplete = false
            )
        }

        /** 创建系统消息 */
        fun createSystem(chatId: Long, content: String): ChatMessage {
            return ChatMessage(
                chatId = chatId,
                role = MessageRole.SYSTEM,
                content = content,
                isComplete = true
            )
        }

        /** 创建工具消息 */
        fun createTool(chatId: Long, toolResults: List<ToolResult>): ChatMessage {
            return ChatMessage(
                chatId = chatId,
                role = MessageRole.TOOL,
                content = "",
                toolResults = Json.encodeToString(toolResults),
                isComplete = true
            )
        }
    }
}

/** 消息角色枚举 */
enum class MessageRole(@SerialName var value: String) {
    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system"),
    TOOL("tool"),
    DEVELOPER("developer")
}

/** 工具调用数据类 */
@Serializable
data class ToolCall(
    val id: String,
    val type: String = "function",
    val function: FunctionCall
)

@Serializable
data class FunctionCall(
    val name: String,
    val arguments: String
)

/** 工具调用结果 */
@Serializable
data class ToolResult(
    val toolCallId: String,
    val name: String,
    val content: String,
    val isError: Boolean = false
)

/** 消息 UI 状态（用于 Compose 状态管理） */
sealed interface MessageUiState {
    data class Streaming(val message: ChatMessage, val delta: String) : MessageUiState
    data class Complete(val message: ChatMessage) : MessageUiState
    data class Error(val message: ChatMessage, val error: String) : MessageUiState
    data class ToolCalling(val message: ChatMessage, val toolCall: ToolCall) : MessageUiState
    data class ToolResult(val message: ChatMessage, val toolResult: ToolResult) : MessageUiState
}