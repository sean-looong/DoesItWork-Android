package com.seanlooong.doesitwork.widget

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap

/**
 * 用于显示drawable中的xml文件
 */
@SuppressLint("LocalContextResourcesRead")
@Composable
fun painterResourceCompat(@DrawableRes id: Int): Painter {
    val context = LocalContext.current
    val res = context.resources
    val value = remember { TypedValue() }
    res.getValue(id, value, true)
    val path = value.string
    if (path?.endsWith(".xml") == false) {
        return painterResource(id = id)
    }
    val imageBitmap = remember(path, id, context.theme) {
        try {
            ImageBitmap.imageResource(res, id)
        } catch (throwable: Throwable) {
            val drawable: Drawable = ContextCompat.getDrawable(context, id)
                ?: throw IllegalArgumentException("not found drawable, path: $path")
            drawable.toBitmap().asImageBitmap()
        }
    }
    return BitmapPainter(imageBitmap)
}