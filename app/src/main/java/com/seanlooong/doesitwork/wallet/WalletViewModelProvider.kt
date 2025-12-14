package com.seanlooong.doesitwork.wallet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.seanlooong.doesitwork.database.WalletDatabase

object WalletViewModelProvider {
    private var _viewModel: WalletViewModel? = null

    @Composable
    fun getOrCreateViewModel(): WalletViewModel {
        val context = LocalContext.current

        return remember {
            _viewModel ?: run {
                val database = WalletDatabase.getDatabase(context)
                val dao = database.walletDao()
                WalletViewModel(dao).also { _viewModel = it }
            }
        }
    }
}