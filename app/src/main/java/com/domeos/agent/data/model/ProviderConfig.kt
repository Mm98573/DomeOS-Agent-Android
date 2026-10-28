package com.domeos.agent.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.domeos.agent.data.local.converters.Converters
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * AI 提供商配置
 * 对应 WinUI 版本中的 Provider 配置
 */
@Entity(tableName = "providers")
@Serializable
@TypeConverters(Converters::class)
data class ProviderConfig(
    @PrimaryKey
    var id: String = "",

    /** 显示名称 */
    var name: String = "",

    /** 提供商类型 */
    var type: ProviderType = ProviderType.CUSTOM,

    /** API Base URL */
    var baseUrl: String = "",

    /** API Key（加密存储） */
    var apiKey: String = "",

    /** 组织 ID（可选） */
    var organizationId: String? = null,

    /** 默认模型 */
    var defaultModel: String = "",

    /** 支持的模型列表（JSON） */
    var modelsJson: String = "[]",

    /** 是否启用 */
    var isEnabled: Boolean = true,

    /** 请求超时(毫秒) */
    var timeoutMs: Int = 60000,

    /** 最大重试次数 */
    var maxRetries: Int = 3,

    /** 自定义请求头（JSON） */
    var customHeaders: String? = null,

    /** 代理设置（JSON） */
    var proxyConfig: String? = null,

    /** 创建时间 */
    var createdAt: Instant = Instant.now(),

    /** 更新时间 */
    var updatedAt: Instant = Instant.now(),

    /** 排序权重 */
    var sortOrder: Int = 0
) {
    /** 获取模型列表 */
    fun getModels(): List<ModelConfig> {
        return modelsJson.let { Json.decodeFromString<List<ModelConfig>>(it) }
    }

    /** 设置模型列表 */
    fun setModels(models: List<ModelConfig>) {
        modelsJson = Json.encodeToString(models)
    }

    /** 获取自定义请求头 */
    fun getCustomHeaders(): Map<String, String> {
        return customHeaders?.let { Json.decodeFromString<Map<String, String>>(it) } ?: emptyMap()
    }

    /** 设置自定义请求头 */
    fun setCustomHeaders(headers: Map<String, String>) {
        customHeaders = Json.encodeToString(headers)
    }

    /** 验证配置有效性 */
    fun isValid(): Boolean {
        return id.isNotBlank() &&
               name.isNotBlank() &&
               baseUrl.isNotBlank() &&
               (type != ProviderType.CUSTOM || apiKey.isNotBlank())
    }

    companion object {
        /** 预设提供商配置 */
        val PRESETS = mapOf(
            "openai" to ProviderConfig(
                id = "openai",
                name = "OpenAI",
                type = ProviderType.OPENAI,
                baseUrl = "https://api.openai.com/v1",
                defaultModel = "gpt-4o",
                modelsJson = Json.encodeToString(listOf(
                    ModelConfig(id = "gpt-4o", name = "GPT-4o", maxTokens = 128000, supportsVision = true, supportsTools = true),
                    ModelConfig(id = "gpt-4o-mini", name = "GPT-4o Mini", maxTokens = 128000, supportsVision = true, supportsTools = true),
                    ModelConfig(id = "gpt-4-turbo", name = "GPT-4 Turbo", maxTokens = 128000, supportsVision = true, supportsTools = true),
                    ModelConfig(id = "gpt-3.5-turbo", name = "GPT-3.5 Turbo", maxTokens = 16384, supportsTools = true)
                ))
            ),
            "anthropic" to ProviderConfig(
                id = "anthropic",
                name = "Anthropic",
                type = ProviderType.ANTHROPIC,
                baseUrl = "https://api.anthropic.com/v1",
                defaultModel = "claude-3-5-sonnet-20241022",
                modelsJson = Json.encodeToString(listOf(
                    ModelConfig(id = "claude-3-5-sonnet-20241022", name = "Claude 3.5 Sonnet", maxTokens = 200000, supportsVision = true, supportsTools = true),
                    ModelConfig(id = "claude-3-5-haiku-20241022", name = "Claude 3.5 Haiku", maxTokens = 200000, supportsVision = true, supportsTools = true),
                    ModelConfig(id = "claude-3-opus-20240229", name = "Claude 3 Opus", maxTokens = 200000, supportsVision = true, supportsTools = true)
                ))
            ),
            "google" to ProviderConfig(
                id = "google",
                name = "Google",
                type = ProviderType.GOOGLE,
                baseUrl = "https://generativelanguage.googleapis.com/v1beta",
                defaultModel = "gemini-1.5-pro",
                modelsJson = Json.encodeToString(listOf(
                    ModelConfig(id = "gemini-1.5-pro", name = "Gemini 1.5 Pro", maxTokens = 2000000, supportsVision = true, supportsTools = true),
                    ModelConfig(id = "gemini-1.5-flash", name = "Gemini 1.5 Flash", maxTokens = 1000000, supportsVision = true, supportsTools = true),
                    ModelConfig(id = "gemini-1.0-pro", name = "Gemini 1.0 Pro", maxTokens = 32768, supportsTools = true)
                ))
            ),
            "openrouter" to ProviderConfig(
                id = "openrouter",
                name = "OpenRouter",
                type = ProviderType.OPENROUTER,
                baseUrl = "https://openrouter.ai/api/v1",
                defaultModel = "anthropic/claude-3.5-sonnet",
                modelsJson = Json.encodeToString(listOf(
                    ModelConfig(id = "anthropic/claude-3.5-sonnet", name = "Claude 3.5 Sonnet", maxTokens = 200000),
                    ModelConfig(id = "openai/gpt-4o", name = "GPT-4o", maxTokens = 128000),
                    ModelConfig(id = "google/gemini-pro-1.5", name = "Gemini 1.5 Pro", maxTokens = 2000000),
                    ModelConfig(id = "meta-llama/llama-3.1-405b", name = "Llama 3.1 405B", maxTokens = 128000)
                ))
            ),
            "ollama" to ProviderConfig(
                id = "ollama",
                name = "Ollama (本地)",
                type = ProviderType.OLLAMA,
                baseUrl = "http://localhost:11434/v1",
                defaultModel = "llama3.1",
                modelsJson = Json.encodeToString(listOf(
                    ModelConfig(id = "llama3.1", name = "Llama 3.1", maxTokens = 128000, supportsTools = true),
                    ModelConfig(id = "llama3.1:70b", name = "Llama 3.1 70B", maxTokens = 128000, supportsTools = true),
                    ModelConfig(id = "qwen2.5:72b", name = "Qwen 2.5 72B", maxTokens = 128000, supportsTools = true),
                    ModelConfig(id = "codellama:34b", name = "CodeLlama 34B", maxTokens = 16384, supportsTools = true)
                ))
            )
        )

        /** 创建自定义提供商 */
        fun createCustom(
            id: String,
            name: String,
            baseUrl: String,
            apiKey: String = "",
            defaultModel: String = ""
        ): ProviderConfig {
            return ProviderConfig(
                id = id,
                name = name,
                type = ProviderType.CUSTOM,
                baseUrl = baseUrl,
                apiKey = apiKey,
                defaultModel = defaultModel
            )
        }
    }
}

/** 提供商类型 */
enum class ProviderType(@SerialName var value: String) {
    OPENAI("openai"),
    ANTHROPIC("anthropic"),
    GOOGLE("google"),
    OPENROUTER("openrouter"),
    OLLAMA("ollama"),
    AZURE("azure"),
    DEEPSEEK("deepseek"),
    MOONSHOT("moonshot"),
    ZHIPU("zhipu"),
    BAIDU("baidu"),
    ALIYUN("aliyun"),
    TENCENT("tencent"),
    CUSTOM("custom")
}

/** 模型配置 */
@Serializable
data class ModelConfig(
    val id: String,
    val name: String,
    val maxTokens: Int = 4096,
    val supportsVision: Boolean = false,
    val supportsTools: Boolean = false,
    val supportsStreaming: Boolean = true,
    val contextWindow: Int = 4096,
    val inputPricePer1k: Double = 0.0,
    val outputPricePer1k: Double = 0.0,
    val description: String = "",
    val metadata: Map<String, String> = emptyMap()
)

/** 请求配置 */
@Serializable
data class RequestConfig(
    val model: String,
    val temperature: Float = 0.7f,
    val topP: Float = 1.0f,
    val topK: Int = 0,
    val maxTokens: Int = 4096,
    val presencePenalty: Float = 0.0f,
    val frequencyPenalty: Float = 0.0f,
    val stopSequences: List<String> = emptyList(),
    val enableStreaming: Boolean = true,
    val enableTools: Boolean = true,
    val seed: Long? = null,
    val responseFormat: ResponseFormat = ResponseFormat.TEXT,
    val extraParams: Map<String, String> = emptyMap()
) {
    companion object {
        fun default(model: String) = RequestConfig(model = model)
    }
}

/** 响应格式 */
enum class ResponseFormat(@SerialName var value: String) {
    TEXT("text"),
    JSON("json_object"),
    JSON_SCHEMA("json_schema")
}

/** 完整的 Agent 配置 */
@Serializable
data class AgentConfig(
    val providerId: String,
    val modelId: String,
    val systemPrompt: String = "",
    val requestConfig: RequestConfig,
    val enabledTools: List<String> = emptyList(),
    val maxIterations: Int = 10,
    val maxContextMessages: Int = 20,
    val enableMemory: Boolean = true,
    val memoryConfig: MemoryConfig = MemoryConfig()
)

/** 记忆配置 */
@Serializable
data class MemoryConfig(
    val enabled: Boolean = true,
    val maxMemories: Int = 100,
    val summaryThreshold: Int = 10,
    val autoSummarize: Boolean = true
)

/** 应用设置 */
@Entity(tableName = "app_settings")
@Serializable
@TypeConverters(Converters::class)
data class AppSettings(
    @PrimaryKey
    val key: String = "global",

    /** 当前主题模式 */
    var themeMode: ThemeMode = ThemeMode.SYSTEM,

    /** 当前语言 */
    var language: String = "zh_CN",

    /** 默认提供商 ID */
    var defaultProviderId: String = "",

    /** 默认模型 ID */
    var defaultModelId: String = "",

    /** 全局系统提示词 */
    var globalSystemPrompt: String = "",

    /** 是否启用流式输出 */
    var enableStreaming: Boolean = true,

    /** 是否启用工具调用 */
    var enableTools: Boolean = true,

    /** 是否启用 Markdown 渲染 */
    var enableMarkdown: Boolean = true,

    /** 是否启用数学公式渲染 */
    var enableMath: Boolean = true,

    /** 代码块主题 */
    var codeTheme: String = "github-dark",

    /** 字体大小缩放 */
    var fontScale: Float = 1.0f,

    /** 是否显示 Token 统计 */
    var showTokenCount: Boolean = true,

    /** 是否显示推理过程 */
    var showReasoning: Boolean = true,

    /** 是否自动滚动到底部 */
    var autoScroll: Boolean = true,

    /** 是否启用震动反馈 */
    var enableHaptics: Boolean = true,

    /** 是否启用声音 */
    var enableSounds: Boolean = false,

    /** 快捷键配置（JSON） */
    var hotkeysJson: String = "{}",

    /** 启动时自动打开快速访问 */
    var autoOpenQuickAccess: Boolean = false,

    /** 最小化到托盘而非退出 */
    var minimizeToTray: Boolean = true,

    /** 通知权限 */
    var notificationPermission: Boolean = false,

    /** 生物识别锁定 */
    var biometricLock: Boolean = false,

    /** 自动更新检查 */
    var autoUpdateCheck: Boolean = true,

    /** 数据保留天数（0=永久） */
    var dataRetentionDays: Int = 0,

    /** 最后同步时间 */
    var lastSyncAt: Instant? = null,

    /** 更新时间 */
    var updatedAt: Instant = Instant.now()
) {
    /** 获取快捷键配置 */
    fun getHotkeys(): Map<String, String> {
        return hotkeysJson.let { Json.decodeFromString<Map<String, String>>(it) }
    }

    /** 设置快捷键配置 */
    fun setHotkeys(hotkeys: Map<String, String>) {
        hotkeysJson = Json.encodeToString(hotkeys)
    }

    companion object {
        val DEFAULT_SYSTEM_PROMPT = """
            你是 DomeOS AI Agent，一个强大、友好、有用的 AI 助手。
            你可以使用工具来完成各种任务，包括但不限于：
            - 网络搜索和信息获取
            - 代码编写和分析
            - 文件操作和数据处理
            - 系统控制和自动化
            
            请始终用中文回复，除非用户明确要求其他语言。
            保持回答简洁、准确、有帮助。
            如果不确定，请诚实说明并询问澄清。
            """.trimIndent()
    }
}

/** 主题模式 */
enum class ThemeMode(@SerialName var value: String) {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system"),
    EXPRESSIVE("expressive")  // Material 3 Expressive 动态主题
}