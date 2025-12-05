package com.seanlooong.doesitwork.database

import android.content.Context
import androidx.databinding.adapters.Converters
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.Date

@Database(
    entities = [WalletCategories::class, WalletTransaction::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WalletDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao

    companion object {
        @Volatile
        private var INSTANCE: WalletDatabase? = null

        fun getDatabase(context: Context): WalletDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WalletDatabase::class.java,
                    "wallet_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// 类型转换器
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromCategoryType(value: String?): WalletCategories.CategoryType? {
        return value?.let { WalletCategories.CategoryType.valueOf(it) }
    }

    @TypeConverter
    fun categoryTypeToString(type: WalletCategories.CategoryType?): String? {
        return type?.name
    }

    @TypeConverter
    fun fromTransactionType(value: String?): WalletTransaction.TransactionType? {
        return value?.let { WalletTransaction.TransactionType.valueOf(it) }
    }

    @TypeConverter
    fun transactionTypeToString(type: WalletTransaction.TransactionType?): String? {
        return type?.name
    }
}

// 数据库回调（用于初始化数据）
class DatabaseCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // 可以在这里插入初始数据
    }
}