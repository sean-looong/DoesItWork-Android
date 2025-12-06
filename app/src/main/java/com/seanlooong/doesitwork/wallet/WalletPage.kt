package com.seanlooong.doesitwork.wallet

import PermissionRequest
import android.Manifest
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.seanlooong.exerciseandroid.ui.widgets.SmallTopAppBar

@Composable
fun WalletPage(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    PermissionRequest(
        requiredPermissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)
    )

    WalletHome(modifier, navController)
}

@Composable
fun WalletHome(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
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
        }
    ) { }
}

@Preview
@Composable
fun WalletHomePreview() {
    WalletHome(navController = rememberNavController())
}