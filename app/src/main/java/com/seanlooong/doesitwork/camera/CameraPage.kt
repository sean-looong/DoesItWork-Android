package com.seanlooong.doesitwork.camera

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavHostController
import com.seanlooong.doesitwork.R
import com.seanlooong.doesitwork.widget.painterResourceCompat
import com.seanlooong.exerciseandroid.ui.widgets.SmallTopAppBar

@Composable
fun CameraPage(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    CameraController(modifier = modifier,
        navController = navController) {

    }
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
