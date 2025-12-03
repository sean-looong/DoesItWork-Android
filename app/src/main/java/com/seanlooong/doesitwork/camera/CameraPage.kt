package com.seanlooong.doesitwork.camera

import PermissionRequest
import android.Manifest
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Environment
import android.view.Display
import android.view.Surface
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.seanlooong.doesitwork.R
import com.seanlooong.doesitwork.utils.painterResourceCompat
import com.seanlooong.exerciseandroid.ui.widgets.SmallTopAppBar
import kotlinx.coroutines.awaitCancellation
import rememberLogger
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPage(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    PermissionRequest(
        requiredPermissions = listOf(
            Manifest.permission.CAMERA)
    )

    CameraPreviewContent(modifier, navController)
}

/**
 * 相机预览界面
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
@UiComposable
private fun CameraPreviewContent(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val context = LocalContext.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val currentRotation by rememberScreenRotation()
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val logger = rememberLogger("CameraPage")
    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )

    fun getOutputDirectory(context: Context): File {
        val mediaDir = context.getExternalFilesDirs(Environment.DIRECTORY_PICTURES).firstOrNull()?.let {
            File(it, context.resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
    }

    fun bindCameraUseCases(
        cameraProvider: ProcessCameraProvider,
        lifecycleOwner: LifecycleOwner,
        rotation: Int,
        preview: Preview? = null,
        imageCapture: ImageCapture? = null
    ) {
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            // 必须先解绑所有用例
            cameraProvider.unbindAll()

            val useCaseGroup = UseCaseGroup.Builder()
                .addUseCase(preview ?: Preview.Builder().build())
                .addUseCase(imageCapture ?: ImageCapture.Builder()
                    .setTargetRotation(rotation)
                    .build())
                .build()

            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                useCaseGroup
            )
        } catch (exc: Exception) {
            logger.e("Use case binding failed", exc)
        }
    }

    fun takePicture(
        context: Context,
        imageCapture: ImageCapture?,
        rotation: Int
    ) {
        imageCapture ?: return

        // 确保旋转设置是最新的
        imageCapture.targetRotation = rotation

        imageCapture.let {
            // 创建输出选项
            val photoFile = File(
                getOutputDirectory(context),
                "${SimpleDateFormat(FILENAME, Locale.CHINA).format(System.currentTimeMillis())}.jpg"
            )

            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            it.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        logger.d("Photo capture failed: ${exc.message}", exc)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = output.savedUri
                        val filePath = photoFile.absolutePath
                        logger.d("Photo capture succeeded: ${savedUri ?: photoFile.absolutePath}")
                    }
                }
            )
        }
    }

    LaunchedEffect(lifecycleOwner) {
        val processCameraProvider = cameraProviderFuture.get()
        val imageCapture = imageCapture ?: return@LaunchedEffect

        // 更新 imageCapture 的旋转设置
        imageCapture.targetRotation = currentRotation

        // 重新绑定以应用更改
        processCameraProvider.unbindAll()
        bindCameraUseCases(processCameraProvider, lifecycleOwner, currentRotation)

        // Cancellation signals we're done with the camera
        try { awaitCancellation() } finally { processCameraProvider.unbindAll() }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                PreviewView(context).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                val executor = ContextCompat.getMainExecutor(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    imageCapture = ImageCapture.Builder()
                        .setTargetRotation(currentRotation)
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture
                        )
                    } catch (exc: Exception) {
                        logger.e("Use case binding failed", exc)
                    }
                }, executor)
            }
        )

        CameraController(modifier, navController, cameraPermissionState) {
            takePicture(context, imageCapture, currentRotation)
        }
    }
}

/**
 * 相机控制组件
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun CameraController(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    permissionState: PermissionState,
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

        if (permissionState.status == PermissionStatus.Granted) {
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
        } else {
            Text(
                text = "請授予拍照權限",
                modifier = Modifier
                    .constrainAs(cameraButton) {
                        bottom.linkTo(parent.bottom, margin = 80.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            )
        }
    }
}

@Composable
private fun rememberScreenRotation(): MutableIntState {
    val context = LocalContext.current
    val view = LocalView.current
    val rotationState = remember { mutableIntStateOf(getCurrentRotation(view)) }

    DisposableEffect(context, view) {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val displayListener = object : DisplayManager.DisplayListener {
            override fun onDisplayChanged(displayId: Int) {
                if (displayId == Display.DEFAULT_DISPLAY) {
                    rotationState.intValue = getCurrentRotation(view)
                }
            }

            override fun onDisplayAdded(displayId: Int) {}
            override fun onDisplayRemoved(displayId: Int) {}
        }

        displayManager.registerDisplayListener(displayListener, null)

        onDispose {
            displayManager.unregisterDisplayListener(displayListener)
        }
    }

    return rotationState
}

fun getCurrentRotation(view: View): Int {
    return view.display?.rotation ?: Surface.ROTATION_0
}

private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
