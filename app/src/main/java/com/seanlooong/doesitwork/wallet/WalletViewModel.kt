package com.seanlooong.doesitwork.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seanlooong.doesitwork.database.TransactionWithCategory
import com.seanlooong.doesitwork.database.WalletCategories
import com.seanlooong.doesitwork.database.WalletCategories.CategoryType
import com.seanlooong.doesitwork.database.WalletDao
import com.seanlooong.doesitwork.database.WalletTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class WalletViewModel(
    private val dao: WalletDao
) : ViewModel() {

    // 收入分类
    private val _categoriesIncome = MutableStateFlow<List<WalletCategories>>(emptyList())
    // 支出分类
    private val _categoriesExpense = MutableStateFlow<List<WalletCategories>>(emptyList())
    val categoriesIncome: StateFlow<List<WalletCategories>> = _categoriesIncome.asStateFlow()
    val categoriesExpense: StateFlow<List<WalletCategories>> = _categoriesExpense.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionWithCategory>>(emptyList())
    val transactions: StateFlow<List<TransactionWithCategory>> = _transactions.asStateFlow()

    init {
        // 加载分类数据
        loadCategories()

        // 加载交易数据
        loadTransactions()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            // 因为代码中的 collect 是一个挂起函数，它会挂起当前协程直到 Flow 完成。
            // 当第一个 collect 执行时，它会一直挂起等待数据，导致第二句代码永远不会执行。
            // 方法1：用launch包裹，分别执行两个协程，
            // 方法2：写两个loadCategories方法，分别调用
            launch {
                dao.getCategoriesByType(CategoryType.INCOME)
                    .catch { exception ->
                        // 处理错误
                        println("加载分类失败: ${exception.message}")
                    }
                    .collect { categories ->
                        _categoriesIncome.value = categories
                    }
            }
            launch {
                dao.getCategoriesByType(CategoryType.EXPENSE)
                    .catch { exception ->
                        // 处理错误
                        println("加载分类失败: ${exception.message}")
                    }
                    .collect { categories ->
                        _categoriesExpense.value = categories
                    }
            }
        }
    }

    private fun loadTransactions(walletId: Long = 1L) {
        viewModelScope.launch {
            dao.getTransactions()
                .catch { exception ->
                    // 处理错误
                    println("加载交易失败: ${exception.message}")
                }
                .collect { transactions ->
                    _transactions.value = transactions
                }
        }
    }

    fun addTransaction(transaction: WalletTransaction) {
        viewModelScope.launch {
            dao.insertTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: WalletTransaction) {
        viewModelScope.launch {
            dao.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            dao.softDeleteTransaction(id)
        }
    }

    fun addCategory(category: WalletCategories) {
        viewModelScope.launch {
            dao.insertCategory(category)
        }
    }
}