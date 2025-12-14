package com.seanlooong.doesitwork.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.seanlooong.doesitwork.DoesItWorkDestinations
import com.seanlooong.exerciseandroid.ui.widgets.SmallTopAppBar

@Composable
fun WalletPage(
    modifier: Modifier = Modifier,
    navController: NavHostController) {

    val viewModel = WalletViewModelProvider.getOrCreateViewModel()

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

        Spacer(modifier = modifier
            .fillMaxSize()
            .weight(1f))
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