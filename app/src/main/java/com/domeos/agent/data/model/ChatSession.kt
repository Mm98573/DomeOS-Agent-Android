package com.domeos.agent.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.domeos.agent.data.local.converters.Converters
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 聊天会话实体
 * 对应 WinUI 版本中的 Chat/Session 数据结构
 */
@Entity(
    tableName = "chat_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ChatSession::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("parentId"),
        Index("updatedAt"),
        Index("isArchived"),
        Index("isPinned")
    ]
)
@Serializable
@TypeConverters(Converters::class)
data class ChatSession(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    /** 父会话 ID（用于分支会话） */
    var parentId: Long? = null,

    /** 会话标题 */
    var title: String = "新建会话",

    /** 系统提示词 */
    var systemPrompt: String = "",

    /** 使用的模型 ID */
    var modelId: String = "",

    /** 提供商 ID */
    var providerId: String = "",

    /** 温度参数 */
    var temperature: Float = 0.7f,

    /** 最大 Token 数 */
    var maxTokens: Int = 4096,

    /** 是否启用流式输出 */
    var enableStreaming: Boolean = true,

    /** 是否启用工具调用 */
    var enableTools: Boolean = true,

    /** 会话图标/表情 */
    var icon: String? = null,

    /** 会话颜色标记 */
    var colorTag: String? = null,

    /** 是否置顶 */
    var isPinned: Boolean = false,

    /** 是否归档 */
    var isArchived: Boolean = false,

    /** 是否收藏 */
    var isFavorite: Boolean = false,

    /** 消息数量 */
    var messageCount: Int = 0,

    /** 总 Token 使用量 */
    var totalTokens: Long = 0,

    /** 创建时间 */
    var createdAt: Instant = Instant.now(),

    /** 更新时间 */
    var updatedAt: Instant = Instant.now(),

    /** 最后一条消息预览 */
    var lastMessagePreview: String = "",

    /** 最后一条消息角色 */
    var lastMessageRole: String = "",

    /** 标签（JSON 数组） */
    var tags: String? = null,

    /** 元数据 */
    var metadata: String? = null
) {
    /** 获取标签列表 */
    fun getTags(): List<String> {
        return tags?.let { Json.decodeFromString<List<String>>(it) } ?: emptyList()
    }

    /** 设置标签列表 */
    fun setTags(tagList: List<String>) {
        tags = Json.encodeToString(tagList)
    }

    /** 获取元数据 */
    fun getMetadata(): Map<String, String> {
        return metadata?.let { Json.decodeFromString<Map<String, String>>(it) } ?: emptyMap()
    }

    /** 设置元数据 */
    fun setMetadata(data: Map<String, String>) {
        metadata = Json.encodeToString(data)
    }

    /** 更新最后消息预览 */
    fun updateLastMessage(message: ChatMessage) {
        lastMessagePreview = when {
            message.content.isNotBlank() -> message.content.take(100)
            message.toolCalls?.isNotBlank() == true -> "🔧 工具调用: ${message.getToolCalls().joinToString(", ") { it.function.name }}"
            message.reasoning?.isNotBlank() == true -> "💭 思考中..."
            else -> "[空消息]"
        }
        lastMessageRole = message.role.value
        updatedAt = Instant.now()
        messageCount++
    }

    companion object {
        /** 创建新会话 */
        fun createNew(
            title: String = "新建会话",
            systemPrompt: String = "",
            modelId: String = "",
            providerId: String = ""
        ): ChatSession {
            return ChatSession(
                title = title,
                systemPrompt = systemPrompt,
                modelId = modelId,
                providerId = providerId
            )
        }
    }
}

/** 会话 UI 状态 */
sealed interface SessionUiState {
    data class Loading : SessionUiState
    data class Loaded(val sessions: List<ChatSession>) : SessionUiState
    data class Error(val throwable: Throwable) : SessionUiState
    data class Empty : SessionUiState
}

/** 会话排序方式 */
enum class SessionSortOrder {
    UPDATED_DESC,  // 最近更新
    UPDATED_ASC,   // 最早更新
    CREATED_DESC,  // 最近创建
    CREATED_ASC,   // 最早创建
    TITLE_ASC,     // 标题 A-Z
    TITLE_DESC,    // 标题 Z-A
    MESSAGE_COUNT  // 消息数量
}

/** 会话过滤器 */
data class SessionFilter(
    val query: String = "",
    val showArchived: Boolean = false,
    val showPinnedOnly: Boolean = false,
    val providerId: String? = null,
    val tags: Set<String> = emptySet(),
    val sortOrder: SessionSortOrder = SessionSortOrder.UPDATED_DESC
)