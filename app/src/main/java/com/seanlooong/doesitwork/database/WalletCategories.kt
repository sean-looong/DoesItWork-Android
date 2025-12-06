package com.seanlooong.doesitwork.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 分类表 (categories) - 收入和支出分类
 */
// 钱包分类表
@Entity(
    tableName = "wallet_categories",
    indices = [Index(value = ["type", "name"], unique = true)]
)
data class WalletCategories(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String, // 分类名称（如：餐饮、交通、工资等）

    @ColumnInfo(name = "type")
    val type: CategoryType, // 分类类型（收入/支出）

    @ColumnInfo(name = "icon")
    val icon: String, // 图标资源名称或URL

    @ColumnInfo(name = "color")
    val color: String, // 颜色值（十六进制）

    @ColumnInfo(name = "is_system")
    val isSystem: Boolean = false, // 是否为系统预设分类

    @ColumnInfo(name = "order_index")
    val orderIndex: Int = 0, // 排序索引

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true, // 是否启用

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    enum class CategoryType {
        INCOME,  // 收入
        EXPENSE  // 支出
    }
}