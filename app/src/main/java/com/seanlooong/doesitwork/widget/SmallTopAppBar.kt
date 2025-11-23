package com.seanlooong.exerciseandroid.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


/**
 * 自定义TopAppBar
 */
@Composable
fun SmallTopAppBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    navigationIcon: @Composable () -> Unit = {},
    title: String = "",
    actionIcon: @Composable () -> Unit = {},
) {
    Column(modifier = Modifier.background(backgroundColor)) {
        Spacer(
            modifier = Modifier
                .windowInsetsTopHeight(WindowInsets.statusBars) //设置状态栏高度
                .fillMaxWidth()
        )
        Row(
            modifier = modifier
                .heightIn()
                .fillMaxWidth()
                .padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.Center, // 水平居中对齐
            verticalAlignment = Alignment.CenterVertically // 垂直居中对齐
        ) {
            Box(
                Modifier
                    .layoutId("navigationIcon")
                    .width(26.dp)
                    .height(26.dp)
            ) {
                CompositionLocalProvider(
                    content = navigationIcon
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(title, fontSize = 18.sp, fontWeight = Bold)
            Spacer(modifier = Modifier.weight(1f))
            Box(
                Modifier
                    .layoutId("actionIcon")
                    .width(26.dp)
                    .height(26.dp)
            ) {
                CompositionLocalProvider(
                    content = actionIcon
                )
            }
        }
    }
}

@Preview
@Composable
fun SmallTopBarPreview() {
    SmallTopAppBar(
        navigationIcon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "back",
            )
        },
        title = "TOP BAR",
        actionIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "search"
            )
        })
}
