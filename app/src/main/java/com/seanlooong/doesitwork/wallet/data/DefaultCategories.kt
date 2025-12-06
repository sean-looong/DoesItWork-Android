package com.seanlooong.doesitwork.wallet.data

import com.seanlooong.doesitwork.database.WalletCategories

// 预设分类数据
object DefaultCategories {
    val expenseCategories = listOf(
        WalletCategories(
            name = "餐饮",
            type = WalletCategories.CategoryType.EXPENSE,
            icon = "restaurant",
            color = "#FF6B6B",
            isSystem = true,
            orderIndex = 1
        ),
        WalletCategories(
            name = "交通",
            type = WalletCategories.CategoryType.EXPENSE,
            icon = "directions_car",
            color = "#4ECDC4",
            isSystem = true,
            orderIndex = 2
        ),
        WalletCategories(
            name = "购物",
            type = WalletCategories.CategoryType.EXPENSE,
            icon = "shopping_cart",
            color = "#FFD166",
            isSystem = true,
            orderIndex = 3
        ),
        WalletCategories(
            name = "游戏",
            type = WalletCategories.CategoryType.EXPENSE,
            icon = "game",
            color = "#06D6A0",
            isSystem = true,
            orderIndex = 4
        ),
        WalletCategories(
            name = "医疗",
            type = WalletCategories.CategoryType.EXPENSE,
            icon = "local_hospital",
            color = "#118AB2",
            isSystem = true,
            orderIndex = 5
        )
    )

    val incomeCategories = listOf(
        WalletCategories(
            name = "工资",
            type = WalletCategories.CategoryType.INCOME,
            icon = "work",
            color = "#073B4C",
            isSystem = true,
            orderIndex = 1
        ),
        WalletCategories(
            name = "兼职",
            type = WalletCategories.CategoryType.INCOME,
            icon = "part_time",
            color = "#EF476F",
            isSystem = true,
            orderIndex = 2
        ),
        WalletCategories(
            name = "投资",
            type = WalletCategories.CategoryType.INCOME,
            icon = "trending_up",
            color = "#118AB2",
            isSystem = true,
            orderIndex = 3
        ),
        WalletCategories(
            name = "红包",
            type = WalletCategories.CategoryType.INCOME,
            icon = "redeem",
            color = "#06D6A0",
            isSystem = true,
            orderIndex = 4
        )
    )

    fun getAll(): List<WalletCategories> = expenseCategories + incomeCategories
}