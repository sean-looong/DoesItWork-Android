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
    // 当前选择的收入分类
    private val _categoryIncomeSelected = MutableStateFlow<WalletCategories?>(null)
    // 当前选择的支出分类
    private val _categoryExpenseSelected = MutableStateFlow<WalletCategories?>(null)
    // 当前选择的分类
    private val _currentCategoryType = MutableStateFlow(CategoryType.EXPENSE)

    val categoriesIncome: StateFlow<List<WalletCategories>> = _categoriesIncome.asStateFlow()
    val categoriesExpense: StateFlow<List<WalletCategories>> = _categoriesExpense.asStateFlow()
    val currentCategoryType: StateFlow<CategoryType> = _currentCategoryType.asStateFlow()
    val categoryIncomeSelected: StateFlow<WalletCategories?> = _categoryIncomeSelected.asStateFlow()
    val categoryExpenseSelected: StateFlow<WalletCategories?> = _categoryExpenseSelected.asStateFlow()

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
                        _categoryIncomeSelected.value = _categoriesIncome.value.first()
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
                        _categoryExpenseSelected.value = _categoriesExpense.value.first()
                    }
            }
        }
    }

    private fun loadTransactions() {
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

    /**
     * 重置选择的分类
     */
    fun resetSelectCategories() {
        _currentCategoryType.value = CategoryType.EXPENSE
        if (_categoriesIncome.value.isNotEmpty()) {
            _categoryIncomeSelected.value = _categoriesIncome.value.first()
        }
        if (_categoriesExpense.value.isNotEmpty()) {
            _categoryExpenseSelected.value = _categoriesExpense.value.first()
        }
    }

    /**
     * 更新当前选择的分类类型
     */
    fun updateCategoryType(type: CategoryType) {
        _currentCategoryType.value = type
    }

    /**
     * 更新选择的收入分类
     */
    fun updateCategoryIncome(category: WalletCategories) {
        _categoryIncomeSelected.value = category
    }

    /**
     * 更新选择的支出分类
     */
    fun updateCategoryExpense(category: WalletCategories) {
        _categoryExpenseSelected.value = category
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