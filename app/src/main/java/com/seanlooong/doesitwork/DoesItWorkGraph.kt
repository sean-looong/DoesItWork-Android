package com.seanlooong.doesitwork

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun DoesItWorkGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = DoesItWorkDestinations.HOME_ROUTE // 默认进入HOME页面
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(
            route = DoesItWorkDestinations.HOME_ROUTE
        ) {
            HomeScreen(modifier.fillMaxSize(), navController)
        }
    }
}