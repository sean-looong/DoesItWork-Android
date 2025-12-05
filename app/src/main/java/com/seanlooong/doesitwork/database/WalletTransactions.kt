package com.seanlooong.doesitwork.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 交易记录表 (transactions) - 核心表
 */
@Entity(
    tableName = "wallet_transactions",
    foreignKeys = [
        ForeignKey(
            entity = WalletCategories::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["date"]),
        Index(value = ["category_id"]),
        Index(value = ["wallet_id"])
    ]
)
data class WalletTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "category_id")
    val categoryId: Long?, // 关联分类ID（可为空，用于未分类记录）

    @ColumnInfo(name = "amount")
    val amount: Double, // 金额（正数为收入，负数为支出）

    @ColumnInfo(name = "currency")
    val currency: String = "CNY", // 货币代码

    @ColumnInfo(name = "description")
    val description: String, // 描述

    @ColumnInfo(name = "date")
    val date: Date, // 交易日期

    @ColumnInfo(name = "time")
    val time: Date, // 交易时间

    @ColumnInfo(name = "type")
    val type: TransactionType, // 交易类型

    @ColumnInfo(name = "payment_method")
    val paymentMethod: String, // 支付方式（现金、信用卡、支付宝等）

    @ColumnInfo(name = "location")
    val location: String?, // 位置信息

    @ColumnInfo(name = "latitude")
    val latitude: Double?, // 纬度信息

    @ColumnInfo(name = "longitude")
    val longitude: Double?, // 经度信息

    @ColumnInfo(name = "attachment")
    val attachment: String?, // 附件路径（如收据图片）

    @ColumnInfo(name = "is_transfer")
    val isTransfer: Boolean = false, // 是否为转账

    @ColumnInfo(name = "transfer_to_wallet_id")
    val transferToWalletId: Long?, // 转账目标钱包ID

    @ColumnInfo(name = "tags")
    val tags: String?, // 标签（JSON格式存储）

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false, // 软删除标记

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
) {
    enum class TransactionType {
        INCOME,     // 收入
        EXPENSE,    // 支出
        TRANSFER    // 转账
    }
}