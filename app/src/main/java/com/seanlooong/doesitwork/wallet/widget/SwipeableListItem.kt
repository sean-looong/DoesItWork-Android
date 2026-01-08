import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@Composable
fun SwipeableListItem(
    index: Int,
    item: String,
    onDelete: () -> Unit = {},
    onArchive: () -> Unit = {},
    onEdit: () -> Unit ={}
) {
    // 使用 animateFloatAsState 管理动画
    var targetOffset by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = targetOffset,
        animationSpec = tween(durationMillis = 300),
        label = "swipeAnimation"
    )

    val maxSwipeDp = (-80).dp // 最大滑动距离
    val maxSwipeDistance = with(LocalDensity.current) {
        maxSwipeDp.toPx()
    }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // 背景层 - 多个按钮
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            horizontalArrangement = Arrangement.End
        ) {
            // 编辑按钮（最右边）
            ActionButton(
                icon = Icons.Default.Edit,
                text = "编辑",
                color = Color.Blue,
                onClick = {
                    targetOffset = 0f
                    onEdit()
                },
                width = 80.dp
            )

            // 归档按钮
            ActionButton(
                icon = Icons.Default.Archive,
                text = "归档",
                color = Color.Green,
                onClick = {
                    targetOffset = 0f
                    onArchive()
                },
                width = 80.dp
            )

            // 删除按钮（最左边）
            ActionButton(
                icon = Icons.Default.Delete,
                text = "删除",
                color = Color.Red,
                onClick = {
                    targetOffset = 0f
                    onDelete()
                },
                width = 80.dp
            )
        }

        // 前景层 - 列表内容
        Card(
            modifier = Modifier
                .offset {
                    IntOffset(
                        animatedOffset.roundToInt(),
                        0
                    )
                }
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            // 判断是否滑动超过一半
                            coroutineScope.launch {
                                targetOffset = if (animatedOffset < maxSwipeDistance * 0.5f) {
                                    maxSwipeDistance
                                } else {
                                    0f
                                }
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            // 实时更新目标值（不带动画）
                            val newOffset = targetOffset + dragAmount
                            targetOffset = newOffset.coerceIn(maxSwipeDistance, 0f)
                        }
                    )
                }
                .clickable {
                    if (targetOffset < 0) {
                        targetOffset = 0f
                    }
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$index.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = item,
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp
                )

                if (animatedOffset < 0) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Swipe",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// 修正 ActionButton 组件
@Composable
fun ActionButton(
    icon: ImageVector,
    text: String,
    color: Color,
    onClick: () -> Unit,
    width: Dp = 80.dp
) {
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .background(color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = text,
                color = Color.White,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}

@Preview
@Composable
fun SwipeableListItemPreview() {
    SwipeableListItem(1, "向左滑动")
}