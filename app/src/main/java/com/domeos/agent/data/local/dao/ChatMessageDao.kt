package com.domeos.agent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import com.domeos.agent.data.model.ChatMessage
import com.domeos.agent.data.model.MessageRole

/**
 * 消息数据访问对象
 */
@Dao
interface ChatMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessage): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<ChatMessage>): List<Long>

    @Update
    suspend fun update(message: ChatMessage): Int

    @Delete
    suspend fun delete(message: ChatMessage): Int

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteByChatId(chatId: Long): Int

    @Query("DELETE FROM messages WHERE chatId = :chatId AND isStreaming = 1")
    suspend fun deleteStreamingMessages(chatId: Long): Int

    @Query("SELECT * FROM messages WHERE id = :id")
    fun getById(id: Long): Flow<ChatMessage?>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getByIdSync(id: Long): ChatMessage?

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt ASC")
    fun getByChatId(chatId: Long): Flow<List<ChatMessage>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt ASC")
    suspend fun getByChatIdSync(chatId: Long): List<ChatMessage>

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND role = :role ORDER BY createdAt ASC")
    fun getByChatIdAndRole(chatId: Long, role: MessageRole): Flow<List<ChatMessage>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND isComplete = 0 ORDER BY createdAt DESC LIMIT 1")
    fun getLastStreamingMessage(chatId: Long): Flow<ChatMessage?>

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND isComplete = 0 ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastStreamingMessageSync(chatId: Long): ChatMessage?

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentMessages(chatId: Long, limit: Int): List<ChatMessage>

    @Query("SELECT COUNT(*) FROM messages WHERE chatId = :chatId")
    suspend fun getCountByChatId(chatId: Long): Int

    @Query("SELECT COUNT(*) FROM messages WHERE chatId = :chatId AND role = :role")
    suspend fun getCountByChatIdAndRole(chatId: Long, role: MessageRole): Int

    @Query("SELECT SUM(tokenCount) FROM messages WHERE chatId = :chatId")
    suspend fun getTotalTokens(chatId: Long): Long?

    @Query("SELECT MAX(createdAt) FROM messages WHERE chatId = :chatId")
    suspend fun getLastMessageTime(chatId: Long): Long?

    /** 获取最后 N 条完整消息（用于上下文构建） */
    @Query("""
        SELECT * FROM messages
        WHERE chatId = :chatId
        AND isComplete = 1
        ORDER BY createdAt DESC
        LIMIT :limit
    """)
    suspend fun getLastCompleteMessages(chatId: Long, limit: Int): List<ChatMessage>

    /** 获取包含工具调用的消息 */
    @Query("SELECT * FROM messages WHERE chatId = :chatId AND toolCalls IS NOT NULL AND toolCalls != '' ORDER BY createdAt ASC")
    suspend fun getToolMessages(chatId: Long): List<ChatMessage>

    /** 批量更新流式消息内容 */
    @Query("UPDATE messages SET content = :content, updatedAt = :updatedAt, tokenCount = :tokenCount WHERE id = :id")
    suspend fun updateStreamingContent(id: Long, content: String, updatedAt: Long, tokenCount: Int): Int

    /** 标记流式消息完成 */
    @Query("UPDATE messages SET isStreaming = 0, isComplete = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markComplete(id: Long, updatedAt: Long): Int

    /** 设置流式消息错误 */
    @Query("UPDATE messages SET isStreaming = 0, isComplete = 1, error = :error, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setError(id: Long, error: String, updatedAt: Long): Int

    /** 更新工具调用 */
    @Query("UPDATE messages SET toolCalls = :toolCalls, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateToolCalls(id: Long, toolCalls: String, updatedAt: Long): Int

    /** 更新工具结果 */
    @Query("UPDATE messages SET toolResults = :toolResults, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateToolResults(id: Long, toolResults: String, updatedAt: Long): Int

    /** 更新推理内容 */
    @Query("UPDATE messages SET reasoning = :reasoning, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateReasoning(id: Long, reasoning: String, updatedAt: Long): Int
}