package com.seanlooong.doesitwork

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.seanlooong.exerciseandroid.ui.widgets.SmallTopAppBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    var backHandTime by remember { mutableStateOf(0L) }
    val density = LocalDensity.current
    val snackBarHostState  = remember { SnackbarHostState() }
    // 在 Composable 中创建协程作用域
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // 处理返回事件
    BackHandler(enabled = true) {
        if (System.currentTimeMillis() - backHandTime < 2000) {
            // 2秒内第二次点击，退出应用
            if (context is Activity) {
                context.finishAffinity()
            }
        } else {
            backHandTime = System.currentTimeMillis()
            coroutineScope.launch {
                snackBarHostState.showSnackbar(
                    message = "再滑动一次退出",
                    duration = SnackbarDuration.Short)
            }
        }
    }

    Scaffold(
        snackbarHost = {
            // 使用 Box 将 Snackbar 定位到顶部
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = WindowInsets.statusBars.getTop(density).dp * 0.3f), // 留出状态栏空间
                contentAlignment = Alignment.TopCenter
            ) {
                SnackbarHost(hostState = snackBarHostState)
            }
        },
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SmallTopAppBar(
                modifier = modifier,
                title = "Android練習"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding),
        ) {
            HomeCard(modifier, "相機") {
                navController.navigate(DoesItWorkDestinations.CAMERA_ROUTE)
            }
        }
    }
}

@Composable
fun HomeCard(
    modifier: Modifier = Modifier,
    text: String = "",
    onClick: () -> Unit = {}
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable(enabled = true, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = modifier.padding(start = 16.dp, end = 16.dp),
            text = text)
    }
}