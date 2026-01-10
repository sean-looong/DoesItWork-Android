package com.seanlooong.doesitwork.wallet

import SwipeableListItem
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.seanlooong.doesitwork.DoesItWorkDestinations
import com.seanlooong.exerciseandroid.ui.widgets.SmallTopAppBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletPage(
    modifier: Modifier = Modifier,
    navController: NavHostController) {

    val viewModel = WalletViewModelProvider.getOrCreateViewModel()
    viewModel.resetSelectCategories()

    val transactions by viewModel.transactions.collectAsState()

    Column(modifier = modifier
        .fillMaxSize()
        .background((Color.White))) {
        SmallTopAppBar(
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back"
                    )
                }
            },
            title = "賬本"
        )

        // 使用 LazyColumn 显示列表
        LazyColumn(
            modifier = Modifier.fillMaxSize().weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(transactions.size) { index ->
                val transaction = transactions[index].transaction
                SwipeableListItem(
                    index = index + 1,
                    item = "${transactions[index].categoryName} ${transaction.amount} ${timestampToString(transaction.time)}",
                    onDelete = {
                        viewModel.deleteTransaction(transactions[index].transaction.id)
                    }
                )
            }
        }

        Row(modifier = modifier.fillMaxWidth()) {
            Spacer(modifier = modifier
                .fillMaxWidth()
                .weight(1f))
            TextButton(onClick = {
                navController.navigate(DoesItWorkDestinations.WALLET_ADD_ROUTE)
            }) {
                Text("添加")
            }
            Spacer(modifier = modifier
                .fillMaxWidth()
                .weight(1f))
        }
    }
}

// 方法1：使用 SimpleDateFormat
fun timestampToString(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
