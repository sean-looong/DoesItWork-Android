import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

@Composable
fun PermissionRequest(
    requiredPermissions: List<String>,
) {
    val context = LocalContext.current
    var showRationale by remember { mutableStateOf(false) }
    var showPermissionDenied by remember { mutableStateOf(false) }
    var showFirstTimeRationale by remember { mutableStateOf(false) }
    var firstTimePermissions by remember { mutableStateOf<List<String>>(emptyList()) }
    val dataStoreManager = DataStoreManager.getInstance()

    // 权限请求后的检查 - 先声明
    fun checkPermissionsAfterRequest() {
        val permissionState = PermissionState(requiredPermissions, context, dataStoreManager)
        when {
            permissionState.hasAllPermissions -> {
                showRationale = false
                showPermissionDenied = false
                showFirstTimeRationale = false
            }
            permissionState.permanentlyDeniedPermissions.isNotEmpty() -> {
                showPermissionDenied = true
                showRationale = false
                showFirstTimeRationale = false
            }
            else -> {
                showRationale = false
                showPermissionDenied = false
                showFirstTimeRationale = false
            }
        }
    }

    // 标记权限为已请求过
    fun markPermissionsAsRequested(permissions: List<String>) {
        permissions.forEach { permission ->
            dataStoreManager.putBooleanSync("permission_$permission", false)
        }
    }

    // 权限启动器
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        // 在权限请求结果返回时标记权限为已请求过
        markPermissionsAsRequested(requiredPermissions)
        checkPermissionsAfterRequest()
    }

    // 请求权限
    fun requestPermissions() {
        val permissionState = PermissionState(requiredPermissions, context, dataStoreManager)
        val permissionsToRequest = permissionState.deniedPermissions.toTypedArray()
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest)
        } else {
            // 如果没有需要请求的权限，直接检查状态
            checkPermissionsAfterRequest()
        }
    }

    // 检查权限状态
    fun checkPermissions() {
        val permissionState = PermissionState(requiredPermissions, context, dataStoreManager)
        when {
            // 所有权限都已授予
            permissionState.hasAllPermissions -> {
                showRationale = false
                showPermissionDenied = false
                showFirstTimeRationale = false
            }
            // 检查是否需要显示解释或有些权限被永久拒绝
            permissionState.hasPartialPermissions -> {
                val permanentlyDenied = permissionState.permanentlyDeniedPermissions
                val shouldShowRationale = permissionState.shouldShowRationale
                val firstTimePerms = permissionState.firstTimePermissions

                when {
                    // 第一次请求权限（最高优先级）
                    firstTimePerms.isNotEmpty() -> {
                        firstTimePermissions = firstTimePerms
                        showFirstTimeRationale = true
                        showRationale = false
                        showPermissionDenied = false
                    }
                    // 需要显示解释（用户之前拒绝过）
                    shouldShowRationale -> {
                        showRationale = true
                        showFirstTimeRationale = false
                        showPermissionDenied = false
                    }
                    // 有权限被永久拒绝（最低优先级）
                    permanentlyDenied.isNotEmpty() -> {
                        showPermissionDenied = true
                        showRationale = false
                        showFirstTimeRationale = false
                    }
                }
            }
            // 第一次申请 - 直接请求权限（不在这里标记，在结果回调中标记）
            else -> {
                requestPermissions()
            }
        }
    }

    // 初始检查
    LaunchedEffect(Unit) {
        checkPermissions()
    }

    // 根据状态显示不同的UI
    when {
        showFirstTimeRationale -> {
            FirstTimePermissionDialog(
                firstTimePermissions = firstTimePermissions,
                onDismiss = {
                    showFirstTimeRationale = false
                },
                onRequestPermission = {
                    showFirstTimeRationale = false
                    requestPermissions()
                }
            )
        }
        showRationale -> {
            val rationalePermissions = PermissionState(requiredPermissions, context, dataStoreManager).rationalePermissions
            PermissionRationaleDialog(
                rationalePermissions = rationalePermissions,
                onDismiss = { showRationale = false },
                onRequestPermission = {
                    showRationale = false
                    requestPermissions()
                }
            )
        }
        showPermissionDenied -> {
            val deniedPermissions = PermissionState(requiredPermissions, context, dataStoreManager).permanentlyDeniedPermissions
            PermissionDeniedDialog(
                deniedPermissions = deniedPermissions,
                onDismiss = { showPermissionDenied = false },
                onGoToSettings = {
                    openAppSettings(context)
                    showPermissionDenied = false
                }
            )
        }
        else -> {
            // 什么都不显示，等待权限检查完成
        }
    }
}

// 权限项数据类
data class PermissionItem(
    val permission: String,
    val isFirstTime: Boolean,
    val isGranted: Boolean,
    val shouldShowRationale: Boolean
)

// PermissionState 类
class PermissionState(
    private val permissions: List<String>,
    private val context: Context,
    private val dataStoreManager: DataStoreManager
) {
    private val permissionItems: List<PermissionItem> by lazy {
        permissions.map { permission ->
            PermissionItem(
                permission = permission,
                isFirstTime = dataStoreManager.getBooleanSync("permission_$permission", true),
                isGranted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED,
                shouldShowRationale = if (context is androidx.activity.ComponentActivity) {
                    ActivityCompat.shouldShowRequestPermissionRationale(context, permission)
                } else {
                    false
                }
            )
        }
    }

    val hasAllPermissions: Boolean
        get() = permissionItems.all { it.isGranted }

    val shouldShowRationale: Boolean
        get() = permissionItems.any { !it.isGranted && it.shouldShowRationale }

    val permanentlyDeniedPermissions: List<String>
        get() = permissionItems.filter {
            !it.isGranted && !it.shouldShowRationale
        }.map { it.permission }

    val grantedPermissions: List<String>
        get() = permissionItems.filter { it.isGranted }.map { it.permission }

    val deniedPermissions: List<String>
        get() = permissionItems.filter { !it.isGranted }.map { it.permission }

    // 需要显示解释的权限（用户之前拒绝过）
    val rationalePermissions: List<String>
        get() = permissionItems.filter {
            !it.isGranted && it.shouldShowRationale
        }.map { it.permission }

    // 第一次请求的权限
    val firstTimePermissions: List<String>
        get() = permissionItems.filter {
            !it.isGranted && it.isFirstTime && !it.shouldShowRationale
        }.map { it.permission }

    // 是否有部分权限需要处理
    val hasPartialPermissions: Boolean
        get() = firstTimePermissions.isNotEmpty() ||
                rationalePermissions.isNotEmpty() ||
                permanentlyDeniedPermissions.isNotEmpty()
}

// 打开应用设置
fun openAppSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e2: Exception) {
            e2.printStackTrace()
        }
    }
}

// 获取权限列表的中文名称
fun getPermissionChineseNames(permissions: List<String>): List<String> {
    return permissions.map { getPermissionChineseName(it) }
}

// 获取单个权限的中文名称
fun getPermissionChineseName(permission: String): String {
    return permissionNameMap[permission] ?: run {
        val permissionName = permission.substringAfterLast(".").lowercase()
        when {
            permissionName.contains("camera") -> "相机"
            permissionName.contains("location") -> "位置"
            permissionName.contains("storage") -> "存储"
            permissionName.contains("contact") -> "联系人"
            permissionName.contains("phone") -> "电话"
            permissionName.contains("sms") -> "短信"
            permissionName.contains("calendar") -> "日历"
            permissionName.contains("sensor") -> "传感器"
            permissionName.contains("microphone") -> "麦克风"
            permissionName.contains("audio") -> "音频"
            permissionName.contains("notification") -> "通知"
            permissionName.contains("bluetooth") -> "蓝牙"
            permissionName.contains("wifi") -> "WiFi"
            permissionName.contains("body") -> "身体传感器"
            permissionName.contains("activity") -> "活动识别"
            else -> permissionName.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }
    }
}

// 权限名称中英文映射
private val permissionNameMap = mapOf(
    android.Manifest.permission.CAMERA to "相机",
    android.Manifest.permission.RECORD_AUDIO to "麦克风",
    android.Manifest.permission.ACCESS_FINE_LOCATION to "精确位置",
    android.Manifest.permission.ACCESS_COARSE_LOCATION to "大致位置",
    android.Manifest.permission.READ_EXTERNAL_STORAGE to "读取存储",
    android.Manifest.permission.WRITE_EXTERNAL_STORAGE to "写入存储",
    android.Manifest.permission.READ_CONTACTS to "读取联系人",
    android.Manifest.permission.CALL_PHONE to "拨打电话",
    android.Manifest.permission.READ_CALENDAR to "读取日历",
    android.Manifest.permission.SEND_SMS to "发送短信",
    android.Manifest.permission.BODY_SENSORS to "身体传感器",
    android.Manifest.permission.POST_NOTIFICATIONS to "通知"
)

// 第一次权限请求对话框
@Composable
fun FirstTimePermissionDialog(
    firstTimePermissions: List<String>,
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit
) {
    val permissionNames = getPermissionChineseNames(firstTimePermissions)
    val permissionText = permissionNames.joinToString("、")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("需要权限") },
        text = {
            Column {
                Text("为了提供完整的功能体验，我们需要以下权限：")
                Text(permissionText, style = androidx.compose.ui.text.TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
                Text("请允许这些权限以继续使用应用功能。")
            }
        },
        confirmButton = {
            TextButton(onClick = onRequestPermission) {
                Text("继续")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

// 权限解释对话框
@Composable
fun PermissionRationaleDialog(
    rationalePermissions: List<String>,
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit
) {
    val permissionNames = getPermissionChineseNames(rationalePermissions)
    val permissionText = permissionNames.joinToString("、")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("需要权限") },
        text = {
            Column {
                Text("此功能需要以下权限才能正常工作：")
                Text(permissionText, style = androidx.compose.ui.text.TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
                Text("请授予这些权限以继续使用完整功能。")
            }
        },
        confirmButton = {
            TextButton(onClick = onRequestPermission) {
                Text("授予权限")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

// 权限被永久拒绝对话框
@Composable
fun PermissionDeniedDialog(
    deniedPermissions: List<String>,
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit
) {
    val permissionNames = getPermissionChineseNames(deniedPermissions)
    val permissionText = permissionNames.joinToString("、")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("权限被拒绝") },
        text = {
            Column {
                Text("以下权限被永久拒绝，需要在应用设置中手动开启：")
                Text(permissionText, style = androidx.compose.ui.text.TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
                Text("请前往设置页面授予这些权限，否则部分功能可能无法正常使用。")
            }
        },
        confirmButton = {
            TextButton(onClick = onGoToSettings) {
                Text("去设置")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
