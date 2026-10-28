package com.domeos.agent.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * OpenAI 兼容 API 请求/响应 DTO
 */
@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Float = 0.7f,
    val top_p: Float = 1.0f,
    val top_k: Int = 0,
    val max_tokens: Int? = null,
    val presence_penalty: Float = 0.0f,
    val frequency_penalty: Float = 0.0f,
    val stop: List<String>? = null,
    val stream: Boolean = false,
    val tools: List<Tool>? = null,
    val tool_choice: Any? = null,
    val seed: Long? = null,
    val response_format: ResponseFormat? = null,
    val user: String? = null
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String? = null,
    val name: String? = null,
    val tool_calls: List<ToolCall>? = null,
    val tool_call_id: String? = null
) {
    companion object {
        fun system(content: String) = ChatMessage(role = "system", content = content)
        fun user(content: String) = ChatMessage(role = "user", content = content)
        fun assistant(content: String) = ChatMessage(role = "assistant", content = content)
        fun tool(content: String, toolCallId: String) = ChatMessage(role = "tool", content = content, tool_call_id = toolCallId)
        fun assistantWithTools(content: String?, toolCalls: List<ToolCall>) = ChatMessage(role = "assistant", content = content, tool_calls = toolCalls)
    }
}

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

@Serializable
data class Tool(
    val type: String = "function",
    val function: FunctionDefinition
)

@Serializable
data class FunctionDefinition(
    val name: String,
    val description: String,
    val parameters: Map<String, Any>
)

@Serializable
data class ResponseFormat(
    val type: String = "text",
    val json_schema: JsonSchema? = null
)

@Serializable
data class JsonSchema(
    val name: String,
    val schema: Map<String, Any>,
    val strict: Boolean = false
)

/** 流式响应 */
@Serializable
data class ChatStreamResponse(
    val id: String,
    val object: String = "chat.completion.chunk",
    val created: Long,
    val model: String,
    val choices: List<StreamChoice>,
    val usage: Usage? = null
)

@Serializable
data class StreamChoice(
    val index: Int,
    val delta: Delta,
    val finish_reason: String? = null
)

@Serializable
data class Delta(
    val role: String? = null,
    val content: String? = null,
    val tool_calls: List<ToolCallDelta>? = null
)

@Serializable
data class ToolCallDelta(
    val index: Int,
    val id: String? = null,
    val type: String? = null,
    val function: FunctionCallDelta? = null
)

@Serializable
data class FunctionCallDelta(
    val name: String? = null,
    val arguments: String? = null
)

/** 完整响应 */
@Serializable
data class ChatResponse(
    val id: String,
    val object: String = "chat.completion",
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage? = null
)

@Serializable
data class Choice(
    val index: Int,
    val message: ChatMessage,
    val finish_reason: String? = null
)

@Serializable
data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

/** 模型列表响应 */
@Serializable
data class ModelsResponse(
    val object: String = "list",
    val data: List<ModelInfo>
)

@Serializable
data class ModelInfo(
    val id: String,
    val object: String = "model",
    val created: Long,
    val owned_by: String,
    val permission: List<Any>? = null,
    val root: String? = null,
    val parent: String? = null
)

/** 错误响应 */
@Serializable
data class ApiError(
    val error: ErrorDetail
)

@Serializable
data class ErrorDetail(
    val message: String,
    val type: String,
    val param: String? = null,
    val code: String? = null
)

/** Anthropic API 格式 */
@Serializable
data class AnthropicRequest(
    val model: String,
    val messages: List<AnthropicMessage>,
    val system: String? = null,
    val max_tokens: Int,
    val temperature: Float = 0.7f,
    val top_p: Float = 1.0f,
    val top_k: Int = 0,
    val stream: Boolean = false,
    val tools: List<AnthropicTool>? = null,
    val tool_choice: Any? = null
)

@Serializable
data class AnthropicMessage(
    val role: String,
    val content: List<AnthropicContent>
) {
    companion object {
        fun user(text: String) = AnthropicMessage(role = "user", content = listOf(AnthropicContent.text(text)))
        fun assistant(text: String) = AnthropicMessage(role = "assistant", content = listOf(AnthropicContent.text(text)))
    }
}

@Serializable
sealed class AnthropicContent {
    @Serializable
    data class Text(val type: String = "text", val text: String) : AnthropicContent()
    @Serializable
    data class ToolUse(
        val type: String = "tool_use",
        val id: String,
        val name: String,
        val input: Map<String, Any>
    ) : AnthropicContent()
    @Serializable
    data class ToolResult(
        val type: String = "tool_result",
        val tool_use_id: String,
        val content: String,
        val is_error: Boolean = false
    ) : AnthropicContent()

    companion object {
        fun text(text: String) = Text(text = text)
        fun toolUse(id: String, name: String, input: Map<String, Any>) = ToolUse(id = id, name = name, input = input)
        fun toolResult(toolUseId: String, content: String, isError: Boolean = false) = ToolResult(tool_use_id = toolUseId, content = content, is_error = isError)
    }
}

@Serializable
data class AnthropicTool(
    val name: String,
    val description: String,
    val input_schema: Map<String, Any>
)

@Serializable
data class AnthropicResponse(
    val id: String,
    val type: String = "message",
    val role: String = "assistant",
    val content: List<AnthropicContent>,
    val model: String,
    val stop_reason: String? = null,
    val stop_sequence: String? = null,
    val usage: AnthropicUsage
)

@Serializable
data class AnthropicUsage(
    val input_tokens: Int,
    val output_tokens: Int
)

@Serializable
data class AnthropicStreamResponse(
    val type: String,
    val message: AnthropicResponse? = null,
    val delta: AnthropicDelta? = null,
    val index: Int = 0
)

@Serializable
data class AnthropicDelta(
    val type: String? = null,
    val text: String? = null,
    val partial_json: String? = null
)

/** Google Gemini API 格式 */
@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val system_instruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig? = null,
    val safetySettings: List<GeminiSafetySetting>? = null,
    val tools: List<GeminiTool>? = null
)

@Serializable
data class GeminiContent(
    val role: String,
    val parts: List<GeminiPart>
)

@Serializable
sealed class GeminiPart {
    @Serializable
    data class Text(val text: String) : GeminiPart()
    @Serializable
    data class FunctionCall(
        val function_call: FunctionCallData
    ) : GeminiPart()
    @Serializable
    data class FunctionResponse(
        val function_response: FunctionResponseData
    ) : GeminiPart()
}

@Serializable
data class FunctionCallData(
    val name: String,
    val args: Map<String, Any>
)

@Serializable
data class FunctionResponseData(
    val name: String,
    val response: Map<String, Any>
)

@Serializable
data class GeminiGenerationConfig(
    val temperature: Float = 0.7f,
    val topP: Float = 1.0f,
    val topK: Int = 0,
    val maxOutputTokens: Int = 4096,
    val stopSequences: List<String>? = null,
    val responseMimeType: String? = null
)

@Serializable
data class GeminiSafetySetting(
    val category: String,
    val threshold: String
)

@Serializable
data class GeminiTool(
    val functionDeclarations: List<GeminiFunctionDeclaration>
)

@Serializable
data class GeminiFunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: Map<String, Any>
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>,
    val usageMetadata: GeminiUsageMetadata? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent,
    val finishReason: String? = null,
    val index: Int = 0,
    val safetyRatings: List<Any>? = null
)

@Serializable
data class GeminiUsageMetadata(
    val promptTokenCount: Int,
    val candidatesTokenCount: Int,
    val totalTokenCount: Int
)