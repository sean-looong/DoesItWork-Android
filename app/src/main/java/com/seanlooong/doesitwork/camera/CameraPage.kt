package com.seanlooong.doesitwork.camera

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.seanlooong.doesitwork.R
import com.seanlooong.doesitwork.utils.PermissionRequest
import com.seanlooong.doesitwork.utils.painterResourceCompat
import com.seanlooong.exerciseandroid.ui.widgets.SmallTopAppBar

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPage(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    PermissionRequest(
        permissions = listOf(
            Manifest.permission.CAMERA),
        rationale = "需要相机权限来拍照",
        deniedMessage = "权限被拒绝，无法使用拍照功能",
        contentHasPermissions = {
            CameraController(modifier = modifier,
                navController = navController) {

            }
        },
        contentNoPermissions = {
            CameraNoContent(modifier = modifier,
                navController = navController)
        }
    )
}



/**
 * 相机控制组件
 */
@Composable
private fun CameraController(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    takePhoto: () -> Unit = {}
) {
    ConstraintLayout(
        modifier = modifier.fillMaxSize()
    ) {
        val (topBar, cameraButton) = createRefs()

        SmallTopAppBar(
            modifier = modifier
                .constrainAs(topBar){
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            backgroundColor = Color.Transparent,
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back"
                    )
                }
            },
            title = "相機"
        )

        /**
         * 拍摄按钮
         */
        Image(
            painter = painterResourceCompat(id = R.drawable.ic_shutter),
            contentDescription = "take photo",
            modifier = Modifier
                .constrainAs(cameraButton) {
                    bottom.linkTo(parent.bottom, margin = 80.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .size(60.dp)
                .clickable(enabled = true, onClick = takePhoto)
        )
    }
}

/**
 * 相机控制组件
 */
@Composable
private fun CameraNoContent(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        SmallTopAppBar(
            backgroundColor = Color.Transparent,
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back"
                    )
                }
            },
            title = "相機"
        )

        Text(
            text = "请授予拍摄权限",
            color = Color.Red,
            modifier = Modifier
                .padding(top = 40.dp)

        )
    }
}
