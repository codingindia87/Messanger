package com.codingindia.messanger.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.codingindia.messanger.core.dao.ConversationDao
import com.codingindia.messanger.core.dao.MessageDao
import com.codingindia.messanger.core.dao.UserDao
import com.codingindia.messanger.features.home.domain.User
import com.codingindia.messanger.features.message.domain.Converters
import com.codingindia.messanger.features.message.domain.Messages

@Database(
    entities = [User::class, Messages::class], version = 3, exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun messageDap(): MessageDao

    abstract fun conversationDao(): ConversationDao

}

