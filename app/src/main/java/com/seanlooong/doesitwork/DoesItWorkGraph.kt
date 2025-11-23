package com.seanlooong.doesitwork

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.seanlooong.doesitwork.camera.CameraPage

@Composable
fun DoesItWorkGraph(
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
            HomePage(navController = navController)
        }
        composable(
            route = DoesItWorkDestinations.CAMERA_ROUTE
        ) {
            CameraPage(navController = navController)
        }
    }
}