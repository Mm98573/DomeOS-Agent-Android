package com.domeos.agent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import com.domeos.agent.data.model.ChatSession
import com.domeos.agent.data.model.SessionFilter
import com.domeos.agent.data.model.SessionSortOrder

/**
 * 会话数据访问对象
 */
@Dao
interface ChatSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: ChatSession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<ChatSession>): List<Long>

    @Update
    suspend fun update(session: ChatSession): Int

    @Delete
    suspend fun delete(session: ChatSession): Int

    @Query("DELETE FROM chat_sessions WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM chat_sessions WHERE isArchived = 1")
    suspend fun deleteAllArchived(): Int

    @Query("DELETE FROM chat_sessions")
    suspend fun deleteAll(): Int

    @Query("SELECT * FROM chat_sessions WHERE id = :id")
    fun getById(id: Long): Flow<ChatSession?>

    @Query("SELECT * FROM chat_sessions WHERE id = :id")
    suspend fun getByIdSync(id: Long): ChatSession?

    @Query("SELECT * FROM chat_sessions WHERE parentId = :parentId ORDER BY updatedAt DESC")
    fun getChildren(parentId: Long): Flow<List<ChatSession>>

    @Transaction
    @Query("SELECT * FROM chat_sessions WHERE parentId IS NULL ORDER BY updatedAt DESC")
    fun getRootSessions(): Flow<List<ChatSession>>

    @Query("SELECT * FROM chat_sessions WHERE isArchived = 0 AND parentId IS NULL ORDER BY updatedAt DESC")
    fun getActiveSessions(): Flow<List<ChatSession>>

    @Query("SELECT * FROM chat_sessions WHERE isArchived = 1 AND parentId IS NULL ORDER BY updatedAt DESC")
    fun getArchivedSessions(): Flow<List<ChatSession>>

    @Query("SELECT * FROM chat_sessions WHERE isPinned = 1 AND isArchived = 0 AND parentId IS NULL ORDER BY updatedAt DESC")
    fun getPinnedSessions(): Flow<List<ChatSession>>

    @Query("SELECT * FROM chat_sessions WHERE providerId = :providerId AND isArchived = 0 AND parentId IS NULL ORDER BY updatedAt DESC")
    fun getByProvider(providerId: String): Flow<List<ChatSession>>

    @Query("SELECT * FROM chat_sessions WHERE title LIKE '%' || :query || '%' AND isArchived = 0 AND parentId IS NULL ORDER BY updatedAt DESC")
    fun searchByTitle(query: String): Flow<List<ChatSession>>

    @Query("SELECT * FROM chat_sessions WHERE lastMessagePreview LIKE '%' || :query || '%' AND isArchived = 0 AND parentId IS NULL ORDER BY updatedAt DESC")
    fun searchByContent(query: String): Flow<List<ChatSession>>

    @Query("SELECT COUNT(*) FROM chat_sessions WHERE isArchived = 0 AND parentId IS NULL")
    suspend fun getActiveCount(): Int

    @Query("SELECT COUNT(*) FROM chat_sessions WHERE isArchived = 1")
    suspend fun getArchivedCount(): Int

    @Query("UPDATE chat_sessions SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: Long, archived: Boolean): Int

    @Query("UPDATE chat_sessions SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean): Int

    @Query("UPDATE chat_sessions SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean): Int

    @Query("UPDATE chat_sessions SET title = :title, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTitle(id: Long, title: String, updatedAt: Long): Int

    @Query("UPDATE chat_sessions SET messageCount = messageCount + 1, lastMessagePreview = :preview, lastMessageRole = :role, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateLastMessage(id: Long, preview: String, role: String, updatedAt: Long): Int

    @Query("UPDATE chat_sessions SET totalTokens = totalTokens + :tokens WHERE id = :id")
    suspend fun incrementTokens(id: Long, tokens: Long): Int

    /** 带过滤器的查询 */
    @Query("""
        SELECT * FROM chat_sessions
        WHERE parentId IS NULL
        AND (:showArchived OR isArchived = 0)
        AND (:showPinnedOnly = 0 OR isPinned = 1)
        AND (:providerId IS NULL OR providerId = :providerId)
        AND (
            :query = '' OR
            title LIKE '%' || :query || '%' OR
            lastMessagePreview LIKE '%' || :query || '%'
        )
        ORDER BY
            CASE WHEN :sortOrder = 0 THEN updatedAt END DESC,
            CASE WHEN :sortOrder = 1 THEN updatedAt END ASC,
            CASE WHEN :sortOrder = 2 THEN createdAt END DESC,
            CASE WHEN :sortOrder = 3 THEN createdAt END ASC,
            CASE WHEN :sortOrder = 4 THEN title END ASC,
            CASE WHEN :sortOrder = 5 THEN title END DESC,
            CASE WHEN :sortOrder = 6 THEN messageCount END DESC
    """)
    fun getFiltered(
        showArchived: Boolean,
        showPinnedOnly: Boolean,
        providerId: String?,
        query: String,
        sortOrder: Int
    ): Flow<List<ChatSession>>

    /** 获取标签统计 */
    @Query("SELECT DISTINCT tags FROM chat_sessions WHERE tags IS NOT NULL AND tags != '' AND tags != '[]'")
    suspend fun getAllTags(): List<String>
}