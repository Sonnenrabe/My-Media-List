package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GameDarkSurfaceHighlight
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * High-performance, draggable fast scrollbar for Compose LazyColumn.
 * Allows quick scrubbing through hundreds of items with an interactive thumb
 * and live index / position indicator bubble.
 */
@Composable
fun FastScrollbar(
    lazyListState: LazyListState,
    totalItemsCount: Int,
    modifier: Modifier = Modifier,
    accentColor: Color = PrimaryIndigo,
    labelProvider: ((itemIndex: Int) -> String)? = null
) {
    if (totalItemsCount <= 4) return

    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    var isDragging by remember { mutableStateOf(false) }
    var dragYOffset by remember { mutableFloatStateOf(0f) }

    val firstVisibleIndex by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }
    val visibleItemsCount by remember { derivedStateOf { lazyListState.layoutInfo.visibleItemsInfo.size } }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .width(36.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        val totalHeightPx = with(density) { maxHeight.toPx() }
        val minThumbHeightPx = with(density) { 44.dp.toPx() }

        // Dynamic thumb height based on proportion of visible items
        val thumbHeightPx = remember(totalHeightPx, totalItemsCount, visibleItemsCount) {
            val ratio = if (totalItemsCount > 0) visibleItemsCount.toFloat() / totalItemsCount else 1f
            (totalHeightPx * ratio).coerceIn(minThumbHeightPx, totalHeightPx * 0.8f)
        }

        val maxScrollRange = (totalHeightPx - thumbHeightPx).coerceAtLeast(1f)

        // Compute thumb Y position
        val thumbYOffsetPx = if (isDragging) {
            dragYOffset.coerceIn(0f, maxScrollRange)
        } else {
            val progress = (firstVisibleIndex.toFloat() / (totalItemsCount - 1).coerceAtLeast(1))
            (progress * maxScrollRange).coerceIn(0f, maxScrollRange)
        }

        val currentTargetIndex = remember(thumbYOffsetPx, maxScrollRange, totalItemsCount) {
            val fraction = (thumbYOffsetPx / maxScrollRange).coerceIn(0f, 1f)
            (fraction * (totalItemsCount - 1)).roundToInt().coerceIn(0, totalItemsCount - 1)
        }

        // Draggable & Tappable Touch Zone
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(36.dp)
                .pointerInput(totalItemsCount, maxScrollRange) {
                    detectTapGestures { offset ->
                        val targetThumbY = (offset.y - thumbHeightPx / 2f).coerceIn(0f, maxScrollRange)
                        val fraction = (targetThumbY / maxScrollRange).coerceIn(0f, 1f)
                        val targetIndex = (fraction * (totalItemsCount - 1)).roundToInt().coerceIn(0, totalItemsCount - 1)
                        coroutineScope.launch {
                            lazyListState.scrollToItem(targetIndex)
                        }
                    }
                }
                .pointerInput(totalItemsCount, maxScrollRange) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragYOffset = (offset.y - thumbHeightPx / 2f).coerceIn(0f, maxScrollRange)
                            val fraction = (dragYOffset / maxScrollRange).coerceIn(0f, 1f)
                            val targetIndex = (fraction * (totalItemsCount - 1)).roundToInt().coerceIn(0, totalItemsCount - 1)
                            coroutineScope.launch {
                                lazyListState.scrollToItem(targetIndex)
                            }
                        },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragYOffset = (dragYOffset + dragAmount.y).coerceIn(0f, maxScrollRange)
                            val fraction = (dragYOffset / maxScrollRange).coerceIn(0f, 1f)
                            val targetIndex = (fraction * (totalItemsCount - 1)).roundToInt().coerceIn(0, totalItemsCount - 1)
                            coroutineScope.launch {
                                lazyListState.scrollToItem(targetIndex)
                            }
                        }
                    )
                }
        ) {
            // Background subtle track line
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
                    .fillMaxHeight()
                    .width(3.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
            )

            // Draggable Thumb Indicator
            Box(
                modifier = Modifier
                    .offset { IntOffset(x = 0, y = thumbYOffsetPx.roundToInt()) }
                    .align(Alignment.TopEnd)
                    .padding(end = 2.dp)
                    .width(if (isDragging) 8.dp else 5.dp)
                    .height(with(density) { thumbHeightPx.toDp() })
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isDragging) accentColor else accentColor.copy(alpha = 0.65f))
            )

            // Floating Preview Bubble while dragging
            AnimatedVisibility(
                visible = isDragging,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset {
                        IntOffset(
                            x = with(density) { (-44).dp.roundToPx() },
                            y = (thumbYOffsetPx - 10f).roundToInt().coerceAtLeast(0)
                        )
                    }
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GameDarkSurfaceHighlight,
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor)
                ) {
                    val label = labelProvider?.invoke(currentTargetIndex)
                        ?: "${currentTargetIndex + 1} / $totalItemsCount"

                    Text(
                        text = label,
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
