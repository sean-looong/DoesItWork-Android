package com.seanlooong.doesitwork.wallet

import PermissionRequest
import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.seanlooong.doesitwork.data.SnackbarMessage
import com.seanlooong.doesitwork.database.WalletCategories
import com.seanlooong.exerciseandroid.ui.widgets.SmallTopAppBar
import kotlinx.coroutines.launch

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

    val density = LocalDensity.current
    val viewModel = WalletViewModelProvider.getOrCreateViewModel()
    val snackbarState by viewModel.snackbarState.collectAsState()
    val snackbarHostState  = remember { SnackbarHostState() }

    // 监听 Snackbar 状态变化
    LaunchedEffect(snackbarState) {
        snackbarState?.let { message ->
            val result = snackbarHostState.showSnackbar(
                message = message.message,
                actionLabel = message.actionLabel,
                duration = message.duration
            )

            if (result == SnackbarResult.ActionPerformed) {
                message.onAction?.invoke()
            }

            // 清空状态
            viewModel.clearSnackbar()
        }
    }

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
        },
        snackbarHost = {
            // 使用 Box 将 Snackbar 定位到顶部
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = WindowInsets.statusBars.getTop(density).dp * 0.3f), // 留出状态栏空间
                contentAlignment = Alignment.TopCenter
            ) {
                SnackbarHost(hostState = snackbarHostState)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding),
        ) {
            WalletCategoriesPan(
                modifier = modifier
                    .fillMaxHeight()
                    .weight(1f),
                viewModel = viewModel)
            WalletKeyBoard(viewModel = viewModel)
        }
    }
}

@Composable
fun WalletCategoriesPan(
    modifier: Modifier = Modifier,
    viewModel: WalletViewModel
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
                    onClick = {
                        selectedTabIndex = 0
                        viewModel.updateCategoryType(WalletCategories.CategoryType.EXPENSE)
                    },
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
                    onClick = { selectedTabIndex = 1
                        viewModel.updateCategoryType(WalletCategories.CategoryType.INCOME)},
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
        0 -> {
            WalletCategoriesExpenseSelector(
                modifier, WalletCategories.CategoryType.EXPENSE, viewModel)
        }
        1 -> {
            WalletCategoriesExpenseSelector(
                modifier, WalletCategories.CategoryType.INCOME, viewModel)
        }
    }
}

@Composable
fun WalletCategoriesExpenseSelector(
    modifier: Modifier = Modifier,
    categoryType: WalletCategories.CategoryType,
    viewModel: WalletViewModel
) {
    val categories by if (categoryType == WalletCategories.CategoryType.EXPENSE)
        viewModel.categoriesExpense.collectAsState() else viewModel.categoriesIncome.collectAsState()
    val selectedCategory by if (categoryType == WalletCategories.CategoryType.EXPENSE)
        viewModel.categoryExpenseSelected.collectAsState() else viewModel.categoryIncomeSelected.collectAsState()

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
            val category = categories[it]
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (selectedCategory == category) Color.Blue else Color.LightGray,
                        CircleShape
                    )
                    .selectable(
                        selected = (selectedCategory == category),
                        onClick = {
                            if (categoryType == WalletCategories.CategoryType.EXPENSE) {
                                viewModel.updateCategoryExpense(category)
                            } else {
                                viewModel.updateCategoryIncome(category)
                            }
                        },
                        role = Role.RadioButton
                    ),
                contentAlignment = Alignment.Center
            ) {
                RadioButton(
                    selected = (selectedCategory == category),
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
    modifier: Modifier = Modifier,
    viewModel: WalletViewModel
) {
    var amount by remember { mutableStateOf("0.00") }
    var num1 by remember { mutableStateOf("0.00") }
    var sign by remember { mutableStateOf("") }
    var num2 by remember { mutableStateOf("0.00") }
    var description by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val keys = listOf<String>(
        "7", "8", "9", "日期",
        "4", "5", "6", "+",
        "1", "2", "3", "-",
        ".", "0", "后退", "完成")

    /**
     * 处理点击日期操作
     */
    fun onClickCalender() {

    }

    /**
     * 将num1 sign num2进行拼接
     */
    fun settAmount() {
        if (num2 != "0.00") {
            amount = num1 + sign + num2
        } else if (sign != "") {
            amount = num1 + sign
        } else {
            amount = num1
        }
    }

    /**
     * 对数字进行拼接
     */
    fun contactNum(num: String, input: String): String {
        if (num == "0.00") {
            return if (input == ".") "0." else input
        } else if (num.contains(".")) {
            return if (input == ".") num else num + input
        } else {
            return num + input
        }
    }

    /**
     * 对数值进行计算
     */
    fun calculateNumbers(): String {
        val double1 = num1.toDoubleOrNull() ?: 0.00
        val double2 = num2.toDoubleOrNull() ?: 0.00

        if (sign == "+") {
            return (double1 + double2).toCleanDecimalString()
        } else if (sign == "-") {
            return (double1 - double2).toCleanDecimalString()
        } else {
            return num1
        }
    }

    /**
     * 点击后退按钮
     */
    fun onClickBack() {
        if (num2 != "0.00") {
            num2 = num2.dropLast(1)
            if (num2.isEmpty()) {
                num2 = "0.00"
            }
        } else if (sign != "") {
            sign = ""
        } else if (num1 != "0.00") {
            num1 = num1.dropLast(1)
            if (num1.isEmpty()) {
                num1 = "0.00"
            }
        }

        settAmount()
    }

    /**
     * 处理点击完成按钮
     */
    fun onClickComplete() {
        num1 = calculateNumbers()
        sign = ""
        num2 = "0.00"
        settAmount()

        val result: Double = (num1.toDoubleOrNull() ?: 0f) as Double
        if (result <= 0) {
            viewModel.showSnackbar(
                SnackbarMessage("金额必须大于0")
            )
        } else {

        }
    }

    /**
     * 处理点击符号操作
     */
    fun onClickSing(input: String) {
        if (num2 != "0.00") {
            num1 = calculateNumbers()
            num2 = "0.00"
        }

        sign = input

        settAmount()
    }

    fun onClickNum(input: String) {
        if (sign != "") {
            num2 = contactNum(num2, input)
        } else {
            num1 = contactNum(num1, input)
        }

        settAmount()
    }


    /**
     * 处理点击事件
     */
    fun onClickButton(text: String) {
        when(text) {
            "日期" -> onClickCalender()
            "后退" -> onClickBack()
            "完成" -> onClickComplete()
            "+", "-" -> onClickSing(text)
            else -> onClickNum(text)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        // 显示输入的金额大小
        Text("CN ${amount}",
            modifier = Modifier
                .border(
                    width = 1.dp,
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                )
                .fillMaxWidth()
                .height(34.dp)
                .wrapContentHeight(Alignment.CenterVertically)
                .padding(start = 10.dp, end = 10.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            fontSize = 16.sp,
            textAlign = TextAlign.End)

        Spacer(modifier = Modifier.height(16.dp))

        BasicTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .border(
                    width = 1.dp,
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done // 搜索按钮
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    // 执行搜索逻辑
                    keyboardController?.hide()
                }
            ),
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp),
                    contentAlignment = Alignment.CenterStart // 左对齐且垂直居中
                ) {
                    if (description.isEmpty()) {
                        Text(
                            text = "请输入",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.wrapContentHeight(Alignment.CenterVertically).padding(start = 10.dp, end = 10.dp)
                        )
                    }
                    // 关键：给 innerTextField 添加相同的 padding, 让提示文字和输入文字对齐
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .padding(horizontal = 10.dp), // 相同的水平padding
                        contentAlignment = Alignment.CenterStart
                    ) {
                        innerTextField()
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        FlowRow (
            modifier = modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 4
        ) {
            repeat(keys.size) {
                Button(
                    onClick = { onClickButton(keys[it]) },
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

fun Double.toCleanDecimalString(): String {
    // 使用 BigDecimal 处理精度问题
    val bigDecimal = this.toBigDecimal()

    return if (bigDecimal.scale() <= 0 ||
        bigDecimal.stripTrailingZeros().scale() <= 0) {
        // 没有小数部分或小数部分全是0
        bigDecimal.toLong().toString()
    } else {
        // 去掉末尾的0
        bigDecimal.stripTrailingZeros().toPlainString()
    }
}

@Preview
@Composable
fun WalletCategoriesPanPreview() {
    val viewModel = WalletViewModelProvider.getOrCreateViewModel()
    WalletCategoriesPan(viewModel = viewModel)
}

@Preview
@Composable
fun WalletKeyBoardPreview() {
    val viewModel = WalletViewModelProvider.getOrCreateViewModel()
    WalletKeyBoard(viewModel = viewModel)
}