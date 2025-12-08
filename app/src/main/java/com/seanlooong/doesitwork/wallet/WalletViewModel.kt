package com.seanlooong.doesitwork.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seanlooong.doesitwork.database.TransactionWithCategory
import com.seanlooong.doesitwork.database.WalletCategories
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

    private val _categories = MutableStateFlow<List<WalletCategories>>(emptyList())
    val categories: StateFlow<List<WalletCategories>> = _categories.asStateFlow()

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
            dao.getAllCategories()
                .catch { exception ->
                    // 处理错误
                    println("加载分类失败: ${exception.message}")
                }
                .collect { categories ->
                    _categories.value = categories
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