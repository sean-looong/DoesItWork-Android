package com.seanlooong.doesitwork.wallet

import PermissionRequest
import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.seanlooong.exerciseandroid.ui.widgets.SmallTopAppBar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WalletAddTransactionPage(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    PermissionRequest(
        requiredPermissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)
    )

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
                title = "添加"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding),
        ) {
            WalletCategoriesPan(
                modifier = modifier
                    .fillMaxHeight()
                    .weight(1f))
            WalletKeyBoard()
        }
    }
}

@Composable
fun WalletCategoriesPan(
    modifier: Modifier = Modifier
) {
    // 当前选中的标签索引 (0=支出, 1=收入)
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // column让tab居中显示
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // surface限制tab的宽度和高度
        Surface(
            modifier = Modifier.width(200.dp).height(40.dp), // 固定宽度
            shape = MaterialTheme.shapes.medium,
            color = Color.Transparent
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.wrapContentSize(),
                containerColor = Color.Transparent // 透明背景
            ) {
                // 支出标签
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = {
                        Text(
                            text = "支出",
                            fontSize = 16.sp,
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
                        )
                    }
                )
                // 收入标签
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = {
                        Text(
                            text = "收入",
                            fontSize = 16.sp,
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
                        )
                    }
                )
            }
        }
    }

    when(selectedTabIndex) {
        0 -> WalletCategoriesExpenseSelector(modifier)
        1 -> WalletCategoriesIncomeSelector(modifier)
    }
}

@Composable
fun WalletCategoriesExpenseSelector(
    modifier: Modifier = Modifier
) {
    val viewModel = WalletViewModelProvider.getOrCreateViewModel()
    // 观察数据
    val categories by viewModel.categoriesExpense.collectAsState()
    val (selectedOption, onOptionSelected) = remember {
        mutableStateOf(categories[0])
    }

    FlowRow (
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 5
    ) {
        repeat(categories.size) {
            val text = categories[it]
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (selectedOption == text) Color.Blue else Color.LightGray,
                        CircleShape
                    )
                    .selectable(
                        selected = (selectedOption == text),
                        onClick = { onOptionSelected(text) },
                        role = Role.RadioButton
                    ),
                contentAlignment = Alignment.Center
            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = null,
                    modifier = Modifier.size(0.dp),
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color.Transparent,
                        unselectedColor = Color.Transparent
                    )
                )
                Text(text = categories[it].name)
            }
        }
    }
}

@Composable
fun WalletCategoriesIncomeSelector(
    modifier: Modifier = Modifier
) {
    val viewModel = WalletViewModelProvider.getOrCreateViewModel()
    // 观察数据
    val categories by viewModel.categoriesIncome.collectAsState()
    val (selectedOption, onOptionSelected) = remember {
        mutableStateOf(categories[0])
    }

    FlowRow (
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 5
    ) {
        repeat(categories.size) {
            val text = categories[it]
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (selectedOption == text) Color.Blue else Color.LightGray,
                        CircleShape
                    )
                    .selectable(
                        selected = (selectedOption == text),
                        onClick = { onOptionSelected(text) },
                        role = Role.RadioButton
                    ),
                contentAlignment = Alignment.Center
            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = null,
                    modifier = Modifier.size(0.dp),
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color.Transparent,
                        unselectedColor = Color.Transparent
                    )
                )
                Text(text = categories[it].name)
            }
        }
    }
}

@Composable
fun WalletKeyBoard(
    modifier: Modifier = Modifier
) {
    val keys = listOf<String>(
        "7", "8", "9", "日期",
        "4", "5", "6", "+",
        "1", "2", "3", "-",
        ".", "0", "后退", "完成")
    Column() {
        FlowRow (
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 4
        ) {
            repeat(keys.size) {
                Button(
                    onClick = {},
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .weight(1f)) {
                    Text(text = keys[it],
                        fontSize = 14.sp,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun WalletCategoriesPanPreview() {
    WalletCategoriesPan()
}

@Preview
@Composable
fun WalletKeyBoardPreview() {
    WalletKeyBoard()
}