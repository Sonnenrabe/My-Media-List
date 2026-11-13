package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Game
import com.example.localization.LocalAppStrings
import com.example.ui.theme.GameDarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextTertiary

/**
 * Compact, non-unfolding archived game card.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArchivedGameCard(
    game: Game,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    isBeingDragged: Boolean = false,
    dragHandleModifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val borderColor by animateColorAsState(
        targetValue = when {
            isBeingDragged -> NeonCyan
            else -> Color(game.colorHex).copy(alpha = 0.25f)
        },
        label = "borderColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isBeingDragged) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("archived_game_card_${game.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBeingDragged) Color(0xFF16253B) else GameDarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isBeingDragged) 8.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 5.dp, height = 36.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(game.colorHex))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (game.notes.isNotBlank()) game.notes else strings.completedTapDetails,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0A0E17),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Text(
                        text = game.formattedPlaytime(),
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = NeonCyan,
                        modifier = Modifier
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                            .testTag("archived_playtime_text_${game.id}")
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(20.dp)
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .then(dragHandleModifier)
                        .testTag("archived_drag_handle_${game.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = strings.reorderPositionDesc,
                        tint = if (isBeingDragged) NeonCyan else TextTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
