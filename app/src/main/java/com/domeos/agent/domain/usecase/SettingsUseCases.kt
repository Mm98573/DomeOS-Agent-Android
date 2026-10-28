package com.domeos.agent.domain.usecase

import com.domeos.agent.data.model.AppSettings
import com.domeos.agent.data.model.ThemeMode
import com.domeos.agent.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 设置管理用例
 */
@Singleton
class SettingsUseCases @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    /** 获取设置流 */
    fun getSettings(): Flow<AppSettings> = settingsRepository.getSettings()

    /** 获取设置（同步） */
    fun getSettingsSync(): AppSettings? = settingsRepository.getSettingsSync()

    /** 更新主题模式 */
    fun updateThemeMode(mode: ThemeMode) {
        settingsRepository.updateThemeMode(mode.ordinal)
    }

    /** 更新语言 */
    fun updateLanguage(language: String) {
        settingsRepository.updateLanguage(language)
    }

    /** 更新默认提供商 */
    fun updateDefaultProvider(providerId: String) {
        settingsRepository.updateDefaultProvider(providerId)
    }

    /** 更新默认模型 */
    fun updateDefaultModel(modelId: String) {
        settingsRepository.updateDefaultModel(modelId)
    }

    /** 更新全局系统提示词 */
    fun updateGlobalSystemPrompt(prompt: String) {
        settingsRepository.updateGlobalSystemPrompt(prompt)
    }

    /** 更新快捷键 */
    fun updateHotkeys(hotkeys: Map<String, String>) {
        settingsRepository.updateHotkeys(hotkeys)
    }

    /** 更新生物识别锁定 */
    fun updateBiometricLock(enabled: Boolean) {
        settingsRepository.updateBiometricLock(enabled)
    }

    /** 更新最后同步时间 */
    fun updateLastSync(time: Long) {
        settingsRepository.updateLastSync(time)
    }

    /** 重置为默认设置 */
    fun resetToDefaults() {
        val defaults = AppSettings()
        settingsRepository.updateSettings(defaults)
    }

    /** 导出设置为 JSON */
    fun exportSettings(): String {
        val settings = getSettingsSync() ?: AppSettings()
        return kotlinx.serialization.json.Json.encodeToString(settings)
    }

    /** 从 JSON 导入设置 */
    fun importSettings(json: String) {
        try {
            val settings = kotlinx.serialization.json.Json.decodeFromString<AppSettings>(json)
            settingsRepository.updateSettings(settings)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid settings JSON", e)
        }
    }
}