package com.domeos.agent.data.remote

import com.domeos.agent.data.model.ProviderConfig
import com.domeos.agent.data.model.ProviderType
import com.domeos.agent.data.remote.api.AnthropicApi
import com.domeos.agent.data.remote.api.GenericApi
import com.domeos.agent.data.remote.api.GeminiApi
import com.domeos.agent.data.remote.api.OpenAiApi
import com.domeos.agent.util.EncryptionUtil
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API 客户端工厂
 * 根据提供商类型创建对应的 API 客户端
 */
@Singleton
class ApiClientFactory @Inject constructor() {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request()
        chain.proceed(request)
    }

    private fun createOkHttpClient(timeoutMs: Int = 60000): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(timeoutMs.toLong(), java.util.concurrent.TimeUnit.MILLISECONDS)
            .readTimeout(timeoutMs.toLong(), java.util.concurrent.TimeUnit.MILLISECONDS)
            .writeTimeout(timeoutMs.toLong(), java.util.concurrent.TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private fun createRetrofit(baseUrl: String, client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    fun createOpenAiApi(provider: ProviderConfig): OpenAiApi {
        val client = createOkHttpClient(provider.timeoutMs)
        val retrofit = createRetrofit(provider.baseUrl, client)
        return retrofit.create(OpenAiApi::class.java)
    }

    fun createAnthropicApi(provider: ProviderConfig): AnthropicApi {
        val client = createOkHttpClient(provider.timeoutMs)
        val retrofit = createRetrofit(provider.baseUrl, client)
        return retrofit.create(AnthropicApi::class.java)
    }

    fun createGeminiApi(provider: ProviderConfig): GeminiApi {
        val client = createOkHttpClient(provider.timeoutMs)
        val retrofit = createRetrofit(provider.baseUrl, client)
        return retrofit.create(GeminiApi::class.java)
    }

    fun createGenericApi(provider: ProviderConfig): GenericApi {
        val client = createOkHttpClient(provider.timeoutMs)
        val retrofit = createRetrofit(provider.baseUrl, client)
        return retrofit.create(GenericApi::class.java)
    }

    /** 根据提供商类型创建对应的 API 实例 */
    fun createApi(provider: ProviderConfig): Any {
        return when (provider.type) {
            ProviderType.OPENAI,
            ProviderType.OPENROUTER,
            ProviderType.AZURE,
            ProviderType.DEEPSEEK,
            ProviderType.MOONSHOT,
            ProviderType.ZHIPU,
            ProviderType.BAIDU,
            ProviderType.ALIYUN,
            ProviderType.TENCENT,
            ProviderType.CUSTOM,
            ProviderType.OLLAMA -> createOpenAiApi(provider)

            ProviderType.ANTHROPIC -> createAnthropicApi(provider)

            ProviderType.GOOGLE -> createGeminiApi(provider)

            else -> createGenericApi(provider)
        }
    }

    /** 获取解密后的 API Key */
    fun getDecryptedApiKey(provider: ProviderConfig): String {
        return if (provider.apiKey.isNotBlank()) {
            try {
                EncryptionUtil.decrypt(provider.apiKey)
            } catch (e: Exception) {
                provider.apiKey // 如果解密失败，直接使用原值（兼容明文存储）
            }
        } else ""
    }

    /** 构建授权头 */
    fun buildAuthHeader(provider: ProviderConfig): String {
        val apiKey = getDecryptedApiKey(provider)
        return when (provider.type) {
            ProviderType.ANTHROPIC -> apiKey // Anthropic 使用 x-api-key
            ProviderType.GOOGLE -> apiKey // Gemini 使用 x-goog-api-key
            else -> "Bearer $apiKey"
        }
    }
}