package com.seanlooong.doesitwork.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    // ==== 分类相关操作 ====
    @Insert
    suspend fun insertCategory(category: WalletCategories): Long

    @Update
    suspend fun updateCategory(category: WalletCategories)

    @Delete
    suspend fun deleteCategory(category: WalletCategories)

    @Query("SELECT * FROM wallet_categories WHERE is_active = 1 ORDER BY order_index ASC")
    fun getAllCategories(): Flow<List<WalletCategories>>

    @Query("SELECT * FROM wallet_categories WHERE type = :type AND is_active = 1 ORDER BY order_index ASC")
    fun getCategoriesByType(type: WalletCategories.CategoryType): Flow<List<WalletCategories>>

    @Query("SELECT * FROM wallet_categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): WalletCategories?

    // ==== 交易相关操作 ====
    @Insert
    suspend fun insertTransaction(transaction: WalletTransaction): Long

    @Update
    suspend fun updateTransaction(transaction: WalletTransaction)

    @Query("UPDATE wallet_transactions SET is_deleted = 1 WHERE id = :id")
    suspend fun softDeleteTransaction(id: Long)

    @Query("SELECT * FROM wallet_transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): WalletTransaction?

    @Query(value =
        """
        SELECT t.*, c.name as category_name, c.icon as category_icon, c.color as category_color
        FROM wallet_transactions t
        LEFT JOIN wallet_categories c ON t.category_id = c.id
        WHERE t.is_deleted = 0
        ORDER BY t.time DESC
        LIMIT CASE WHEN :limit > 0 THEN :limit ELSE NULL END
        """
    )
    fun getTransactions(limit: Int = 0): Flow<List<TransactionWithCategory>>

    @Query(value =
        """
        SELECT t.*, c.name as category_name, c.icon as category_icon, c.color as category_color
        FROM wallet_transactions t
        LEFT JOIN wallet_categories c ON t.category_id = c.id
        WHERE t.is_deleted = 0
        AND t.time BETWEEN :startTime AND :endTime
        ORDER BY t.time DESC
        """
    )
    fun getTransactionsByDateRange(
        startTime: Long,
        endTime: Long
    ): Flow<List<TransactionWithCategory>>

    @Query(value =
        """
        SELECT
            strftime('%Y-%m', datetime(time/1000), 'unixepoch', 'localtime') as month,
            type,
            SUM(amount) as total
        FROM wallet_transactions
        WHERE is_deleted = 0
        GROUP BY month, type
        ORDER BY month DESC
        """
    )
    fun getMonthlySummary(): Flow<List<MonthlySummary>>

    @Query(value =
        """
        SELECT
            c.id as categoryId,
            c.name as categoryName,
            c.icon as categoryIcon,
            c.color as categoryColor,
            SUM(t.amount) as totalAmount,
            COUNT(*) as transactionCount
        FROM wallet_transactions t
        JOIN wallet_categories c ON t.category_id = c.id
        WHERE t.is_deleted = 0
        AND t.type = :type
        AND t.time BETWEEN :startTime AND :endTime
        GROUP BY c.id
        ORDER BY totalAmount DESC
        """
    )
    fun getCategorySummary(
        type: WalletTransaction.TransactionType,
        startTime: Long,
        endTime: Long
    ): Flow<List<CategorySummary>>
}

// 查询结果的数据类
data class TransactionWithCategory(
    @Embedded
    val transaction: WalletTransaction,

    @ColumnInfo(name = "category_name")
    val categoryName: String?,

    @ColumnInfo(name = "category_icon")
    val categoryIcon: String?,

    @ColumnInfo(name = "category_color")
    val categoryColor: String?
)

data class MonthlySummary(
    val month: String,
    val type: WalletTransaction.TransactionType,
    val total: Double
)

data class CategorySummary(
    val categoryId: Long,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val totalAmount: Double,
    val transactionCount: Int
)
