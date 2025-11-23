package com.seanlooong.doesitwork.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequest(
    permissions: List<String>,
    rationale: String,
    deniedMessage: String,
    contentHasPermissions: @Composable () -> Unit = {},
    contentNoPermissions: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var showRationale by remember { mutableStateOf(false) }
    var showDeniedDialog by remember { mutableStateOf(false) }

    val permissionState = rememberMultiplePermissionsState(permissions = permissions)
    // 关键：使用单独的标志来跟踪是否已经请求过
    var requestStatus by remember { mutableStateOf(PermissionRequestStatus.INITIAL) }
    var hasRequest by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val grantedPermissions = results.filter { it.value }.keys
        val deniedPermissions = results.filter { !it.value }.keys

        if (deniedPermissions.isEmpty()) {
            // 所有权限都授予了
        } else {
            // 检查是否有权限被永久拒绝
            val permanentlyDeniedPermissions = deniedPermissions.filter { permission ->
                activity?.let {
                    !ActivityCompat.shouldShowRequestPermissionRationale(it, permission)
                } ?: true
            }

            when {
                permanentlyDeniedPermissions.size == deniedPermissions.size -> {
                    // 所有被拒绝的权限都是永久拒绝
                }
                permanentlyDeniedPermissions.isNotEmpty() -> {
                    // 部分权限被永久拒绝
                }
                else -> {
                    // 权限被拒绝，但没有永久拒绝
                }
            }
        }
    }

    // 初始权限检查
    LaunchedEffect(permissionState.permissions) {
        val shouldShowRationaleCount = permissionState.permissions.count { it.status.shouldShowRationale }
        val deniedCount = permissionState.permissions.count { it.status is PermissionStatus.Denied }
        when {
            // 所有权限都已授予
            permissionState.allPermissionsGranted -> {
                // 全部被授权
            }
            (!hasRequest && shouldShowRationaleCount == 0) -> {
                // 第一次请求权限
                permissionLauncher.launch(permissions.toTypedArray())
            }
            (shouldShowRationaleCount > 0) -> {
                showRationale = true
            }
            (deniedCount > 0 && shouldShowRationaleCount == 0) -> {
                showDeniedDialog = true
            }
            else -> {
                permissionLauncher.launch(permissions.toTypedArray())
            }
        }
    }

// 处理权限状态变化
    if (permissionState.allPermissionsGranted) {
        contentHasPermissions()
    } else {
        contentNoPermissions()
        if (showRationale) {
            PermissionRationaleDialog(
                rationale = rationale,
                onConfirm = {
                    showRationale = false
                    permissionLauncher.launch(permissions.toTypedArray())
                },
                onDismiss = {
                    showRationale = false
                }
            )
        }

        if (showDeniedDialog) {
            PermissionDeniedDialog(
                message = deniedMessage,
                onConfirm = {
                    showDeniedDialog = false
                    context.openAppSettings()
                },
                onDismiss = {
                    showDeniedDialog = false
                }
            )
        }
    }
}

@Composable
fun PermissionRationaleDialog(
    rationale: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "权限请求")
        },
        text = {
            Text(text = rationale)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("同意")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("拒绝")
            }
        }
    )
}

@Composable
fun PermissionDeniedDialog(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "权限被拒绝")
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("去设置")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("暂不开启")
            }
        }
    )
}

// 添加 Context 的扩展函数
fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }
    startActivity(intent)
}

enum class PermissionRequestStatus {
    INITIAL,        // 初始状态，还未请求
    REQUESTED,      // 已请求权限，等待结果
    GRANTED,        // 所有权限已授予
    DENIED,         // 权限被拒绝，但可以再次请求
    PARTIALLY_DENIED, // 部分权限被永久拒绝
    PERMANENTLY_DENIED // 所有权限被永久拒绝
}
