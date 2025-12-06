package com.seanlooong.doesitwork.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.seanlooong.doesitwork.database.DatabaseVersions.CURRENT_VERSION

object DatabaseVersions {
    const val VERSION_1 = 1
    const val CURRENT_VERSION = VERSION_1
}

@Database(
    entities = [WalletCategories::class, WalletTransaction::class],
    version = CURRENT_VERSION,
    exportSchema = false
)

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
                    "wallet_database.db"
                )
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
