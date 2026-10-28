package com.domeos.agent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow
import com.domeos.agent.data.model.ProviderConfig

/**
 * 提供商配置数据访问对象
 */
@Dao
interface ProviderConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(provider: ProviderConfig): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(providers: List<ProviderConfig>): List<Long>

    @Update
    suspend fun update(provider: ProviderConfig): Int

    @Delete
    suspend fun delete(provider: ProviderConfig): Int

    @Query("DELETE FROM providers WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("SELECT * FROM providers WHERE id = :id")
    fun getById(id: String): Flow<ProviderConfig?>

    @Query("SELECT * FROM providers WHERE id = :id")
    suspend fun getByIdSync(id: String): ProviderConfig?

    @Query("SELECT * FROM providers WHERE isEnabled = 1 ORDER BY sortOrder ASC, name ASC")
    fun getEnabledProviders(): Flow<List<ProviderConfig>>

    @Query("SELECT * FROM providers ORDER BY sortOrder ASC, name ASC")
    fun getAllProviders(): Flow<List<ProviderConfig>>

    @Query("SELECT * FROM providers WHERE type = :type")
    suspend fun getByType(type: String): ProviderConfig?

    @Query("UPDATE providers SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: String, enabled: Boolean): Int

    @Query("UPDATE providers SET apiKey = :apiKey, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateApiKey(id: String, apiKey: String, updatedAt: Long): Int

    @Query("UPDATE providers SET defaultModel = :model, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateDefaultModel(id: String, model: String, updatedAt: Long): Int

    @Query("UPDATE providers SET modelsJson = :models, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateModels(id: String, models: String, updatedAt: Long): Int

    @Query("UPDATE providers SET sortOrder = :order WHERE id = :id")
    suspend fun updateSortOrder(id: String, order: Int): Int

    /** 初始化预设提供商 */
    @Query("SELECT COUNT(*) FROM providers")
    suspend fun getCount(): Int
}