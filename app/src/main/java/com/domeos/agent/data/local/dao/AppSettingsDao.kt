package com.domeos.agent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.domeos.agent.data.model.AppSettings

/**
 * 应用设置数据访问对象
 */
@Dao
interface AppSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: AppSettings): Long

    @Update
    suspend fun update(settings: AppSettings): Int

    @Query("SELECT * FROM app_settings WHERE key = 'global'")
    fun getSettings(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings WHERE key = 'global'")
    suspend fun getSettingsSync(): AppSettings?

    @Query("UPDATE app_settings SET themeMode = :mode, updatedAt = :updatedAt WHERE key = 'global'")
    suspend fun updateThemeMode(mode: Int, updatedAt: Long): Int

    @Query("UPDATE app_settings SET language = :lang, updatedAt = :updatedAt WHERE key = 'global'")
    suspend fun updateLanguage(lang: String, updatedAt: Long): Int

    @Query("UPDATE app_settings SET defaultProviderId = :id, updatedAt = :updatedAt WHERE key = 'global'")
    suspend fun updateDefaultProvider(id: String, updatedAt: Long): Int

    @Query("UPDATE app_settings SET defaultModelId = :id, updatedAt = :updatedAt WHERE key = 'global'")
    suspend fun updateDefaultModel(id: String, updatedAt: Long): Int

    @Query("UPDATE app_settings SET globalSystemPrompt = :prompt, updatedAt = :updatedAt WHERE key = 'global'")
    suspend fun updateGlobalSystemPrompt(prompt: String, updatedAt: Long): Int

    @Query("UPDATE app_settings SET hotkeysJson = :json, updatedAt = :updatedAt WHERE key = 'global'")
    suspend fun updateHotkeys(json: String, updatedAt: Long): Int

    @Query("UPDATE app_settings SET biometricLock = :enabled, updatedAt = :updatedAt WHERE key = 'global'")
    suspend fun updateBiometricLock(enabled: Boolean, updatedAt: Long): Int

    @Query("UPDATE app_settings SET lastSyncAt = :time, updatedAt = :updatedAt WHERE key = 'global'")
    suspend fun updateLastSync(time: Long, updatedAt: Long): Int
}