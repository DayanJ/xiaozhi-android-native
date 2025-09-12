package com.lhht.aiassistant.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.lhht.aiassistant.database.dao.ConversationDao
import com.lhht.aiassistant.database.dao.MessageDao
import com.lhht.aiassistant.database.dao.DifyConfigDao
import com.lhht.aiassistant.database.dao.XiaozhiConfigDao
import com.lhht.aiassistant.database.entity.ConversationEntity
import com.lhht.aiassistant.database.entity.MessageEntity
import com.lhht.aiassistant.database.entity.DifyConfigEntity
import com.lhht.aiassistant.database.entity.XiaozhiConfigEntity

/**
 * 应用数据库
 */
@Database(
    entities = [
        ConversationEntity::class, 
        MessageEntity::class,
        DifyConfigEntity::class,
        XiaozhiConfigEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun difyConfigDao(): DifyConfigDao
    abstract fun xiaozhiConfigDao(): XiaozhiConfigDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ai_assistant_database"
                )
                .fallbackToDestructiveMigration() // 开发阶段使用，生产环境需要迁移策略
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

