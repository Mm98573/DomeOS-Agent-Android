package com.domeos.agent.data.remote.api

import com.domeos.agent.data.remote.dto.ApiDtos.*
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * OpenAI 兼容 API 接口
 */
interface OpenAiApi {

    @POST("chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") auth: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>

    @Streaming
    @POST("chat/completions")
    suspend fun chatCompletionStream(
        @Header("Authorization") auth: String,
        @Body request: ChatRequest
    ): Response<Flow<String>>

    @GET("models")
    suspend fun listModels(
        @Header("Authorization") auth: String
    ): Response<ModelsResponse>

    /** 兼容自定义 Base URL */
    @POST
    suspend fun chatCompletionCustom(
        @Url url: String,
        @Header("Authorization") auth: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>

    @Streaming
    @POST
    suspend fun chatCompletionStreamCustom(
        @Url url: String,
        @Header("Authorization") auth: String,
        @Body request: ChatRequest
    ): Response<Flow<String>>
}

/**
 * Anthropic API 接口
 */
interface AnthropicApi {

    @Headers(
        "Content-Type: application/json",
        "anthropic-version: 2023-06-01"
    )
    @POST("messages")
    suspend fun createMessage(
        @Header("x-api-key") apiKey: String,
        @Body request: AnthropicRequest
    ): Response<AnthropicResponse>

    @Streaming
    @Headers(
        "Content-Type: application/json",
        "anthropic-version: 2023-06-01"
    )
    @POST("messages")
    suspend fun createMessageStream(
        @Header("x-api-key") apiKey: String,
        @Body request: AnthropicRequest
    ): Response<Flow<String>>
}

/**
 * Google Gemini API 接口
 */
interface GeminiApi {

    @POST("models/{model}:generateContent")
    suspend fun generateContent(
        @Header("x-goog-api-key") apiKey: String,
        @retrofit2.http.Path("model") model: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>

    @Streaming
    @POST("models/{model}:streamGenerateContent")
    suspend fun streamGenerateContent(
        @Header("x-goog-api-key") apiKey: String,
        @retrofit2.http.Path("model") model: String,
        @Body request: GeminiRequest
    ): Response<Flow<String>>

    @GET("models")
    suspend fun listModels(
        @Header("x-goog-api-key") apiKey: String
    ): Response<ModelsResponse>
}

/**
 * 通用 API 接口（用于自定义提供商）
 */
interface GenericApi {

    @POST
    suspend fun chatCompletion(
        @Url url: String,
        @Header("Authorization") auth: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: ChatRequest
    ): Response<ChatResponse>

    @Streaming
    @POST
    suspend fun chatCompletionStream(
        @Url url: String,
        @Header("Authorization") auth: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: ChatRequest
    ): Response<Flow<String>>
}