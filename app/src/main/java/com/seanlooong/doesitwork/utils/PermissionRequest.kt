import android.R
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
    showRationale: Boolean = false,
    showPermanentlyDenied: Boolean = false
) {
    val context = LocalContext.current
    var showRequestPermissions by remember { mutableStateOf(false) }
    var showPermissionDenied by remember { mutableStateOf(false) }
    // 需要被请求的权限
    var needRequestPermissions = listOf<String>()
    // 所有永远被拒绝且不再询问的权限
    val deniedPermissionsPermissions = mutableListOf<String>()
    val dataStoreManager = DataStoreManager.getInstance()

    // 权限请求后的检查 - 先声明
    fun checkPermissionsAfterRequest(result: Map<String, Boolean>) {
        deniedPermissionsPermissions.clear()
        for (requestResult in result) {
            val permission = requestResult.key
            val isGranted = requestResult.value
            if (isGranted) {
                dataStoreManager.putIntSync("permission_$permission", PermissionStates.STATE_REQUESTED)
            } else {
                val shouldShowRationale = if (context is androidx.activity.ComponentActivity) {
                    ActivityCompat.shouldShowRequestPermissionRationale(context, permission)
                } else {
                    false
                }

                if (shouldShowRationale) {
                    dataStoreManager.putIntSync("permission_$permission", PermissionStates.STATE_REQUESTED)
                } else {
                    // 之前已经拒绝且不再询问的权限加入到需要数组中
                    if (dataStoreManager.getIntSync(
                            "permission_$permission",
                            PermissionStates.STATE_PERMANENTLY_DENIED) == PermissionStates.STATE_PERMANENTLY_DENIED) {
                        deniedPermissionsPermissions.add(permission)
                    }
                    dataStoreManager.putIntSync("permission_$permission", PermissionStates.STATE_PERMANENTLY_DENIED)
                }
            }
        }

        // 之前包含了已经拒绝且不再询问的权限，但是需要强制询问
        if (!deniedPermissionsPermissions.isEmpty() && showPermanentlyDenied) {
            showRequestPermissions = false
            showPermissionDenied = true
        }
    }

    // 权限启动器
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        checkPermissionsAfterRequest(results)
    }

    // 请求权限
    fun requestPermissions() {
        val permissionState = PermissionState(requiredPermissions, context, dataStoreManager)
        val permissionsToRequest = permissionState.requestPermissions.toTypedArray()
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest)
        }
    }

    // 检查权限状态
    fun checkPermissions() {
        val permissionState = PermissionState(requiredPermissions, context, dataStoreManager)
        needRequestPermissions = permissionState.requestPermissions
        if (permissionState.hasAllPermissions) {
            // 所有权限都已授予
            showPermissionDenied = false
            showRequestPermissions = false
        } else {
            // 需要请求的权限
            showPermissionDenied = false
            if (showRationale && permissionState.isFirstTimePermissions) {
                showRequestPermissions = true
            } else {
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
        showRequestPermissions -> {
            RequestPermissionDialog(
                requestPermissions = needRequestPermissions,
                onDismiss = {
                    showRequestPermissions = false
                },
                onRequestPermission = {
                    requestPermissions()
                    showRequestPermissions = false
                }
            )
        }
        showPermissionDenied -> {
            PermissionDeniedDialog(
                deniedPermissions = deniedPermissionsPermissions,
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
    val isGranted: Boolean,
    val isFirstTime: Boolean
)

// PermissionState 类
private class PermissionState(
    private val permissions: List<String>,
    private val context: Context,
    private val dataStoreManager: DataStoreManager
) {
    private val permissionItems: List<PermissionItem> = permissions.map { permission ->
        PermissionItem(
            isGranted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED,
            isFirstTime = dataStoreManager.getIntSync(
                "permission_$permission",
                PermissionStates.STATE_INITIAL) == PermissionStates.STATE_INITIAL,
            permission = permission
        )
    }

    // 是否包含所有权限
    val hasAllPermissions: Boolean
        get() = permissionItems.all { it.isGranted }

    // 第一次请求的权限
    val isFirstTimePermissions: Boolean
        get() = permissionItems.any { !it.isGranted && it.isFirstTime }

    // 所有被授予的权限
    val grantedPermissions: List<String>
        get() = permissionItems.filter { it.isGranted }.map { it.permission }

    // 所有需要被授权的权限(不包含不再询问的权限)
    val requestPermissions: List<String>
        get() = permissionItems.filter { !it.isGranted }.map { it.permission }
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
fun RequestPermissionDialog(
    requestPermissions: List<String>,
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit
) {
    val permissionNames = getPermissionChineseNames(requestPermissions)
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

object PermissionStates {
    const val STATE_INITIAL = 0 // 初始化状态 - 从未请求过该权限
    const val STATE_REQUESTED = 1 // 已请求但被拒绝 - 用户拒绝但未选择"不再询问"
    const val STATE_PERMANENTLY_DENIED = 2 // 永久被拒绝 - 用户选择"不再询问"
}
