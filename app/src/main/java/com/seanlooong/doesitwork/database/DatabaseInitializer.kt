package com.seanlooong.doesitwork.database

import android.content.Context
import com.seanlooong.doesitwork.wallet.data.DefaultCategories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

object DatabaseInitializer {

    fun initializeDatabase(context: Context) {
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        val database = WalletDatabase.getDatabase(context)

        // 异步检查并初始化数据
        coroutineScope.launch {
            initializeDefaultDataIfNeeded(database.walletDao())
        }
    }

    private suspend fun initializeDefaultDataIfNeeded(dao: WalletDao) {
        // 检查是否已有分类数据
        val existingCategories = dao.getAllCategories().firstOrNull()

        if (existingCategories == null || existingCategories.isEmpty()) {
            insertDefaultCategories(dao)
        }
    }

    private suspend fun insertDefaultCategories(dao: WalletDao) {
        val defaultCategories = DefaultCategories.getAll()

        defaultCategories.forEach { category ->
            dao.insertCategory(
                category.copy(
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}