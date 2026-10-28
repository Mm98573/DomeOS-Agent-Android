package com.domeos.agent.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.domeos.agent.data.local.dao.AppSettingsDao
import com.domeos.agent.data.local.dao.ChatMessageDao
import com.domeos.agent.data.local.dao.ChatSessionDao
import com.domeos.agent.data.local.dao.ProviderConfigDao
import com.domeos.agent.data.local.converters.Converters
import com.domeos.agent.data.model.AppSettings
import com.domeos.agent.data.model.ChatMessage
import com.domeos.agent.data.model.ChatSession
import com.domeos.agent.data.model.ProviderConfig

/**
 * DomeOS 数据库
 * 版本历史：
 * 1 - 初始版本
 */
@Database(
    entities = [
        ChatSession::class,
        ChatMessage::class,
        ProviderConfig::class,
        AppSettings::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class DomeOSDatabase : RoomDatabase() {

    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun providerConfigDao(): ProviderConfigDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: DomeOSDatabase? = null

        private const val DATABASE_NAME = "domeos.db"

        fun getInstance(context: Context): DomeOSDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DomeOSDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /** 迁移示例：1 -> 2 */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 示例：添加新列
                // database.execSQL("ALTER TABLE chat_sessions ADD COLUMN new_column INTEGER DEFAULT 0")
            }
        }
    }
}